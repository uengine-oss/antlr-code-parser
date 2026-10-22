package legacymodernizer.parser.recovery.workingcopy;

import java.util.Map;
import java.util.TreeSet;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 지문을 뜨기 전에 JSON 의 **키 순서를 우리가 정한다** (2026-10-22).
 *
 * ## 왜 있나
 *
 * 이 저장소에는 JSON 직렬화 결과의 sha256 을 **계약으로 고정**해 둔 자리가
 * 둘 있다 — 언어 카탈로그(`LanguageCatalogValidator`)와 실패 봉투
 * (`FailureEnvelopeFactory`). 둘 다 매퍼의
 * {@code MapperFeature.SORT_PROPERTIES_ALPHABETICALLY} 가 정렬된 JSON 을
 * 내준다는 전제 위에 서 있었다.
 *
 * Boot 4(Jackson 3)로 옮기자 그 기능이 record 에 적용되지 않아 **선언 순서**로
 * 나왔다. 내용은 한 글자도 안 바뀌었는데 지문만 달라져, 카탈로그 쪽은
 * {@code checksum mismatch} 로 **파싱이 통째로 멈췄다**(2026-10-22 실측).
 *
 * 지문이 **직렬화기의 기본값에 매달려 있으면** 라이브러리가 움직일 때마다
 * 같은 일이 난다. 그래서 정렬을 라이브러리에 맡기지 않고 여기서 한다.
 *
 * ⚠ **배열은 건드리지 않는다.** 배열에서는 순서가 데이터 자체다.
 */
public final class CanonicalJson {

    private CanonicalJson() {
    }

    /** 객체 키를 재귀로 정렬한 복사본을 돌려준다. 원본은 그대로 둔다. */
    public static JsonNode sortKeys(ObjectMapper mapper, JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode sorted = mapper.createObjectNode();
            for (String name : new TreeSet<>(node.properties().stream()
                    .map(Map.Entry::getKey).toList())) {
                sorted.set(name, sortKeys(mapper, node.get(name)));
            }
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode copy = mapper.createArrayNode();
            for (JsonNode child : node) {
                copy.add(sortKeys(mapper, child));
            }
            return copy;
        }
        return node;
    }

    /** 키를 정렬한 뒤 sha256 을 뜬다. 계약으로 고정된 지문은 전부 이 길로 온다. */
    public static String sha256(ObjectMapper mapper, JsonNode node) {
        return Hashes.sha256(mapper.writeValueAsBytes(sortKeys(mapper, node)));
    }
}
