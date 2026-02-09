package legacymodernizer.parser.antlr;

import java.util.ArrayList;

/**
 * 파싱 결과를 저장하는 노드 클래스
 * 모든 언어(Java, PL/SQL, PostgreSQL, PL/pgSQL)에서 동일한 속성명 사용
 */
public class Node {
    // ========================================
    // 기본 속성 (모든 노드 공통)
    // ========================================
    public String type;           // 노드 타입 (CLASS, METHOD, PROCEDURE 등)
    public String name;           // 이름 (클래스명, 메서드명, 프로시저명 등)
    public int startLine;         // 시작 라인
    public int endLine;           // 종료 라인
    public String code;           // 원본 소스코드 (라인번호 포함, 줄바꿈 유지)
    
    // ========================================
    // 선언부 관련 (클래스, 메서드, 프로시저/함수)
    // ========================================
    public String signature;      // 선언부 전체 (IS/AS 이전, { 이전 등)
    public String modifiers;      // 수정자 (public, private, static 등)
    public String returnType;     // 리턴 타입
    public String parameters;     // 파라미터 목록
    public String genericType;    // 제네릭 타입 파라미터
    
    // ========================================
    // 상속/구현 관계 (Java)
    // ========================================
    public String extendsType;    // 상속 대상 (클래스, 인터페이스)
    public String implementsTypes; // 구현 대상 (인터페이스 목록)
    
    // ========================================
    // 필드/변수 관련
    // ========================================
    public String fieldType;      // 필드/변수 타입
    
    // ========================================
    // 스키마 관련 (PL/SQL, PostgreSQL)
    // ========================================
    public String schema;         // 스키마명 (schema.procedure_name)
    
    // ========================================
    // 트리 구조
    // ========================================
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
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // 필수 속성
        json.append("\"type\":\"").append(type).append("\"");
        
        // 선택 속성 (null이 아닌 경우만)
        appendJsonField(json, "name", name);
        appendJsonField(json, "signature", signature);
        appendJsonField(json, "modifiers", modifiers);
        appendJsonField(json, "returnType", returnType);
        appendJsonField(json, "parameters", parameters);
        appendJsonField(json, "genericType", genericType);
        appendJsonField(json, "extendsType", extendsType);
        appendJsonField(json, "implementsTypes", implementsTypes);
        appendJsonField(json, "fieldType", fieldType);
        appendJsonField(json, "schema", schema);
        appendJsonField(json, "code", code);
        
        // 라인 정보
        json.append(",\"startLine\":").append(startLine);
        json.append(",\"endLine\":").append(endLine);
        
        // 자식 노드
        json.append(",\"children\":[");
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) json.append(",");
            json.append(children.get(i).toJson());
        }
        json.append("]");
        
        json.append("}");
        return json.toString();
    }
    
    private void appendJsonField(StringBuilder json, String key, String value) {
        if (value != null) {
            json.append(",\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return null;
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
