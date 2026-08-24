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
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type", "name",
    "signature", "modifiers", "annotations", "returnType", "parameters", "genericType",
    "extendsType", "implementsTypes",
    "variableType", "initValue",
    "target", "operator", "expression", "statementOrigin", "conditionTiming",
    "dataObjectEvidenceVersion", "dataObjectReferences", "qualifiedColumnReferences",
    "unqualifiedIdentifierReferences",
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

    // ========================================
    // 구조 statement 표현식 (spec 016)
    // ========================================
    /**
     * grammar 가 그 statement 에 소유시킨 표현식의 원문 텍스트.
     * RETURN/THROW/RAISE 는 반환·예외식, ASSIGNMENT 는 우변, 제어문(IF/LOOP/CASE)은
     * 조건식이다. 값 없는 return 은 RETURN 노드로 남고 이 필드만 null 이다 (FR-004).
     * downstream(analyzer)이 소스를 다시 파싱하지 않기 위한 명시 계약 필드 (FR-003).
     */
    public String expression;
    /**
     * 제어 조건이 본문보다 먼저(pre)인지 뒤(post)인지 나타내는 grammar 사실.
     * null은 기존 계약과 호환되는 pre이다. 현재 post는 C/Java do-while만 생산한다.
     */
    public String conditionTiming;
    /** ASSIGNMENT 의 좌변(lvalue) 원문. 다른 노드에서는 null (FR-003). */
    public String target;
    /** ASSIGNMENT 의 대입 연산자 원문 (`=`, `+=`, `:=` …). 다른 노드에서는 null. */
    public String operator;
    /** Grammar-owned origin of a compatibility statement projection. */
    public String statementOrigin;

    // ========================================
    // 데이터 객체 구문 증거 (PL/SQL spec 017)
    // ========================================
    /** Presence distinguishes the grammar contract from legacy ASTs that require replay fallback. */
    public Integer dataObjectEvidenceVersion;
    /** Grammar-owned physical object references for this DML node. Empty remains absent. */
    public ArrayList<DataObjectReference> dataObjectReferences;
    /** Explicit qualifier.column references owned by this DML node. Empty remains absent. */
    public ArrayList<QualifiedColumnReference> qualifiedColumnReferences;
    /** Grammar-owned unqualified SQL identifiers. No physical table ownership is asserted. */
    public ArrayList<UnqualifiedIdentifierReference> unqualifiedIdentifierReferences;

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
