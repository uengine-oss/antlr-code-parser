package legacymodernizer.parser.service.parsing;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Target별 파싱 전략 팩토리
 * 
 * 지원 타입: java, oracle, postgresql
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParserStrategyFactory {

    private final JavaParserStrategy javaStrategy;
    private final PlSqlParserStrategy plSqlStrategy;
    private final PostgreSqlParserStrategy postgreSqlStrategy;

    private Map<String, TargetParserStrategy> strategies;

    @PostConstruct
    private void init() {
        strategies = Map.of(
                "java", javaStrategy,
                "oracle", plSqlStrategy,
                "plsql", plSqlStrategy,
                "postgresql", postgreSqlStrategy,
                "postgres", postgreSqlStrategy);

        log.info("파서 전략 초기화: {}", strategies.keySet());
    }

    /**
     * target 타입에 맞는 파싱 전략 반환
     */
    public TargetParserStrategy getStrategy(String target) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target이 필요합니다");
        }

        String key = target.trim().toLowerCase();
        TargetParserStrategy strategy = strategies.get(key);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "지원하지 않는 target: " + target + " (가능: " + getSupportedTypes() + ")");
        }

        return strategy;
    }

    public List<String> getSupportedTypes() {
        return List.copyOf(strategies.keySet());
    }
}
