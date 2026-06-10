package legacymodernizer.parser.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.service.strategy.TargetParserStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 언어/방언 자동 감지 — 호출자가 target 을 던지지 않는다.
 *
 * <p>감지 = "거창한 알고리즘"이 아니라 결정론적 규칙:
 * <ol>
 *   <li><b>확장자</b>로 1차 라우팅 (각 전략의 {@code getTargetExtensions()} 에서 역으로 구성 — DRY).
 *       예: {@code .java}→java, {@code .c}→c, {@code .pks/.pkb/.prc/.fnc}→oracle(확정).</li>
 *   <li>{@code .sql} 처럼 <b>여러 전략이 주장하는 애매한 확장자</b>(Oracle PL/SQL vs PostgreSQL)는
 *       파일 <b>내용의 방언 특유 마커를 점수화</b>해 결정. 프로젝트 단위로 합산 → 한 방언.</li>
 *   <li>마커가 전혀 없으면(순수 ANSI SQL) 차상위 폴백(기본 oracle) — 호출자가 데이터소스/사용자로
 *       덮어쓸 수 있도록 결정 결과를 {@link DetectionResult} 로 노출.</li>
 * </ol>
 *
 * <p>전략(파서 문법)은 이미 antlr 이 보유 — 여기서는 "어느 파서로 보낼지"만 정한다.
 */
@Slf4j
@Component
public class LanguageDetector {

    /** 확장자(소문자, '.' 포함) → 그 확장자를 주장하는 전략들 (1개면 확정, 2개+면 방언 판별 대상). */
    private final Map<String, List<TargetParserStrategy>> extToStrategies = new LinkedHashMap<>();
    /** target 타입명 → 전략 (예: "oracle", "postgresql"). */
    private final Map<String, TargetParserStrategy> typeToStrategy = new LinkedHashMap<>();

    public LanguageDetector(List<TargetParserStrategy> strategies) {
        for (TargetParserStrategy s : strategies) {
            typeToStrategy.put(s.getSupportedTargetType().toLowerCase(Locale.ROOT), s);
            for (String ext : s.getTargetExtensions()) {
                extToStrategies.computeIfAbsent(ext.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(s);
            }
        }
        log.info("언어 감지 초기화: 확장자 {} / 타입 {}", extToStrategies.keySet(), typeToStrategy.keySet());
    }

    /** 지원하는 모든 소스 확장자(합집합) — 업로드가 어떤 파일을 소스로 분류·저장할지에 사용. */
    public Set<String> supportedExtensions() {
        return Set.copyOf(extToStrategies.keySet());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 감지 결과
    // ─────────────────────────────────────────────────────────────────────

    /**
     * @param fileStrategies  파일 → 그 파일을 파싱할 전략 (확장자 미지원 파일은 미포함=스킵).
     * @param detectedTargets 감지된 전략 타입 집합 (예: {"oracle"} 또는 {"java","oracle"}).
     * @param sqlDialect      .sql 방언 판별 결과 ("oracle"/"postgresql"), .sql 없으면 null.
     * @param primaryTarget   대표 언어(프론트 sourceType 용) — 선택 strategy 내 파일 최다 타입. 없으면 null.
     * @param strategy        "framework" / "dbms" (robo 파이프라인 선택). robo detect_strategy 와 동일 규칙
     *                        (framework 언어가 하나라도 있으면 framework, SQL만이면 dbms, 없으면 framework).
     */
    public record DetectionResult(
            Map<Path, TargetParserStrategy> fileStrategies,
            Set<String> detectedTargets,
            String sqlDialect,
            String primaryTarget,
            String strategy) {
    }

    // ─────────────────────────────────────────────────────────────────────
    // 감지
    // ─────────────────────────────────────────────────────────────────────

    /** sourceDir 하위 파일들을 훑어 파일별 파서 전략을 결정한다. */
    public DetectionResult detect(Path sourceDir) {
        List<Path> files;
        try {
            files = Files.walk(sourceDir).filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new RuntimeException("소스 디렉토리 탐색 실패: " + sourceDir, e);
        }

        // 1) .sql(애매) 파일을 모아 프로젝트 단위로 방언 1회 결정.
        List<Path> ambiguousSql = new ArrayList<>();
        for (Path f : files) {
            List<TargetParserStrategy> cands = extToStrategies.get(extensionOf(f));
            if (cands != null && cands.size() > 1) ambiguousSql.add(f);
        }
        TargetParserStrategy sqlStrategy = ambiguousSql.isEmpty() ? null : resolveSqlDialect(ambiguousSql);

        // 2) 파일별 전략 라우팅 + 타입별 파일 수 집계(대표 언어 산출용).
        Map<Path, TargetParserStrategy> fileStrategies = new LinkedHashMap<>();
        Set<String> detectedTargets = new LinkedHashSet<>();
        Map<String, Integer> countByType = new LinkedHashMap<>();
        for (Path f : files) {
            List<TargetParserStrategy> cands = extToStrategies.get(extensionOf(f));
            if (cands == null || cands.isEmpty()) continue;            // 미지원 확장자 → 스킵
            TargetParserStrategy s = cands.size() == 1 ? cands.get(0) : sqlStrategy;
            if (s == null) continue;
            fileStrategies.put(f, s);
            String type = s.getSupportedTargetType();
            detectedTargets.add(type);
            countByType.merge(type, 1, Integer::sum);
        }

        String sqlDialect = sqlStrategy == null ? null : sqlStrategy.getSupportedTargetType();
        String strategy = deriveStrategy(detectedTargets);
        String primaryTarget = derivePrimaryTarget(countByType, strategy);
        log.info("언어 감지 결과: targets={}, 대표={}, strategy={}, sql방언={}, 파싱대상 {}/{}개",
                detectedTargets, primaryTarget, strategy, sqlDialect, fileStrategies.size(), files.size());
        return new DetectionResult(fileStrategies, detectedTargets, sqlDialect, primaryTarget, strategy);
    }

    /** dbms 로 분류할 전략 타입 (그 외 모두 = framework 코드). */
    private static final Set<String> DBMS_TYPES = Set.of("oracle", "postgresql");

    /** robo detect_strategy 와 동일 규칙 — framework 언어가 하나라도 있으면 framework, SQL만이면 dbms, 없으면 framework. */
    private static String deriveStrategy(Set<String> targets) {
        if (targets.isEmpty()) return "framework";
        boolean hasFrameworkLang = targets.stream().anyMatch(t -> !DBMS_TYPES.contains(t));
        return hasFrameworkLang ? "framework" : "dbms";
    }

    /** 선택된 strategy 에 속하는 타입 중 파일이 가장 많은 것 = 대표 언어(프론트 sourceType). */
    private static String derivePrimaryTarget(Map<String, Integer> countByType, String strategy) {
        boolean wantDbms = "dbms".equals(strategy);
        return countByType.entrySet().stream()
                .filter(e -> DBMS_TYPES.contains(e.getKey()) == wantDbms)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // SQL 방언 점수 판별 (Oracle PL/SQL vs PostgreSQL)
    // ─────────────────────────────────────────────────────────────────────

    /** 방언 특유 문법 마커 — (정규식, 가중치). 확정적일수록 높은 가중치. */
    private record Marker(Pattern pattern, int weight) {
    }

    private static final List<Marker> ORACLE_MARKERS = List.of(
            new Marker(Pattern.compile("\\bVARCHAR2\\b", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bDBMS_\\w+", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bNVL\\s*\\(", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bSYSDATE\\b", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bFROM\\s+DUAL\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("\\bPACKAGE\\s+BODY\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("\\bEXCEPTION\\s+WHEN\\b", Pattern.CASE_INSENSITIVE), 4),
            new Marker(Pattern.compile("\\bMERGE\\s+INTO\\b", Pattern.CASE_INSENSITIVE), 3),
            new Marker(Pattern.compile("\\bROWNUM\\b", Pattern.CASE_INSENSITIVE), 4),
            new Marker(Pattern.compile("\\bCONNECT\\s+BY\\b", Pattern.CASE_INSENSITIVE), 6),
            new Marker(Pattern.compile("\\b(IS|AS)\\s+BEGIN\\b", Pattern.CASE_INSENSITIVE), 3));

    private static final List<Marker> POSTGRES_MARKERS = List.of(
            new Marker(Pattern.compile("\\$\\$"), 10),
            new Marker(Pattern.compile("\\bLANGUAGE\\s+plpgsql\\b", Pattern.CASE_INSENSITIVE), 10),
            new Marker(Pattern.compile("\\bSERIAL\\b", Pattern.CASE_INSENSITIVE), 6),
            new Marker(Pattern.compile("\\bRETURNS\\s+\\w", Pattern.CASE_INSENSITIVE), 5),
            new Marker(Pattern.compile("\\bRAISE\\s+NOTICE\\b", Pattern.CASE_INSENSITIVE), 8),
            new Marker(Pattern.compile("::\\w"), 3),
            new Marker(Pattern.compile("\\bnow\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE), 3),
            new Marker(Pattern.compile("\\bTEXT\\b\\s*(,|\\)|DEFAULT|NOT)", Pattern.CASE_INSENSITIVE), 2));

    /** 애매한 .sql 파일들의 내용을 합산 점수화 → 높은 방언 전략. 0/동점이면 oracle(레거시 기본). */
    private TargetParserStrategy resolveSqlDialect(List<Path> sqlFiles) {
        int oracle = 0;
        int postgres = 0;
        for (Path f : sqlFiles) {
            String content = readText(f);
            if (content.isEmpty()) continue;
            oracle += score(content, ORACLE_MARKERS);
            postgres += score(content, POSTGRES_MARKERS);
        }

        String chosen = postgres > oracle ? "postgresql" : "oracle";   // 동점/0 → oracle 기본
        log.info("SQL 방언 점수: oracle={}, postgresql={} → {}", oracle, postgres, chosen);
        return typeToStrategy.get(chosen);
    }

    private static int score(String content, List<Marker> markers) {
        int total = 0;
        for (Marker m : markers) {
            java.util.regex.Matcher matcher = m.pattern().matcher(content);
            while (matcher.find()) total += m.weight();
        }
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────────────────────────────

    private static String extensionOf(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot);
    }

    /** UTF-8 우선, 실패 시 EUC-KR (레거시 인코딩 대비). */
    private static String readText(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                return Files.readString(file, Charset.forName("EUC-KR"));
            } catch (Exception e2) {
                log.warn("SQL 방언 감지용 파일 읽기 실패(UTF-8·EUC-KR 모두) — 건너뜀: {}", file);
                return "";
            }
        }
    }
}
