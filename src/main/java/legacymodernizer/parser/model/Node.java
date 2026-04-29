package legacymodernizer.parser.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 파싱 결과를 저장하는 AST 노드.
 * 모든 언어(Java, C, Python, PL/SQL, PostgreSQL)에서 동일한 속성명 사용.
 *
 * null 필드는 JSON에 포함되지 않음 (@JsonInclude).
 * Boolean 플래그는 true일 때만 set, 아니면 null → JSON에서 자동 생략.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type", "name",
    "signature", "modifiers", "annotations", "returnType", "parameters", "genericType",
    "extendsType", "implementsTypes",
    "variableType", "initValue",
    "initializerContainsMethodCall", "initializerContainsNewInstance",
    "schema", "moduleName",
    "fileName", "filePath", "packageName",
    "comment",
    "startLine", "endLine",
    "children"
})
public class Node {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // ========================================
    // 기본 속성 (모든 노드 공통)
    // ========================================
    public String type;
    public String name;
    public int startLine;
    public int endLine;
    public String comment;

    // ========================================
    // 선언부 관련
    // ========================================
    public String signature;
    public String modifiers;
    public String annotations;
    public String returnType;
    public String parameters;
    public String genericType;

    // ========================================
    // 상속/구현 관계 (Java)
    // ========================================
    public String extendsType;
    public String implementsTypes;

    // ========================================
    // 필드/변수 관련
    // ========================================
    public String variableType;
    /** 변수 선언 시 초기화 표현식 텍스트 (예: "tagsn", "DBMS_UTILITY.GET_TIME", "'N'"). 없으면 null. */
    public String initValue;
    /** true일 때만 set, 아니면 null (JSON에서 자동 생략) */
    public Boolean initializerContainsMethodCall;
    /** true일 때만 set, 아니면 null (JSON에서 자동 생략) */
    public Boolean initializerContainsNewInstance;

    // ========================================
    // 스키마 관련 (PL/SQL, PostgreSQL)
    // ========================================
    public String schema;

    // ========================================
    // 소속 모듈
    // ========================================
    public String moduleName;

    // ========================================
    // 파일 관련 (FILE 노드)
    // ========================================
    public String fileName;
    public String filePath;
    public String packageName;

    // ========================================
    // 트리 구조
    // ========================================
    @JsonIgnore
    public Node parent;
    public ArrayList<Node> children = new ArrayList<>();

    public Node(String type, int startLine, Node parent) {
        this(type, null, startLine, parent);
    }

    public Node(String type, String name, int startLine, Node parent) {
        this.type = type;
        this.name = name;
        this.startLine = startLine;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
}
