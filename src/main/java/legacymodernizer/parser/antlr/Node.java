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
    public String comment;        // 선행 주석 (Javadoc 등, 라인번호 포함)
    
    // ========================================
    // 선언부 관련 (클래스, 메서드, 프로시저/함수)
    // ========================================
    public String signature;      // 선언부 전체 (IS/AS 이전, { 이전 등)
    public String modifiers;      // 수정자 (public, private, static 등)
    public String annotations;    // 어노테이션 목록 (@Override, @Deprecated 등)
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
    public String variableType;    // 변수 타입
    /** VARIABLE: 초기화식 문자열에 메서드 호출 패턴(괄호 등)이 있으면 true — 정규식으로 판별 */
    public Boolean initializerContainsMethodCall;
    /** VARIABLE: 초기화식 문자열에 new 타입 패턴이 있으면 true — 정규식으로 판별 */
    public Boolean initializerContainsNewInstance;
    
    // ========================================
    // 스키마 관련 (PL/SQL, PostgreSQL)
    // ========================================
    public String schema;         // 스키마명 (schema.procedure_name)
    
    // ========================================
    // 소속 모듈 관련 (CLASS/INTERFACE 자식 노드)
    // ========================================
    public String moduleName;     // 소속 모듈명 (클래스/파일/구조체 등)
    
    // ========================================
    // 파일 관련 (FILE 노드)
    // ========================================
    public String fileName;       // 파일명 (예: "UserService.java")
    public String filePath;       // 파일 경로 (예: "com/example/service/UserService.java" 또는 전체 경로)
    public String packageName;   // 패키지명 (예: "com.example.service") — FILE 노드 속성
    
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
        appendJsonField(json, "annotations", annotations);
        appendJsonField(json, "returnType", returnType);
        appendJsonField(json, "parameters", parameters);
        appendJsonField(json, "genericType", genericType);
        appendJsonField(json, "extendsType", extendsType);
        appendJsonField(json, "implementsTypes", implementsTypes);
        appendJsonField(json, "variableType", variableType);
        if (Boolean.TRUE.equals(initializerContainsMethodCall)) json.append(",\"initializerContainsMethodCall\":true");
        if (Boolean.TRUE.equals(initializerContainsNewInstance)) json.append(",\"initializerContainsNewInstance\":true");
        appendJsonField(json, "schema", schema);
        appendJsonField(json, "moduleName", moduleName);
        appendJsonField(json, "fileName", fileName);
        appendJsonField(json, "filePath", filePath);
        appendJsonField(json, "packageName", packageName);
        appendJsonField(json, "comment", comment);
        
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
    
    /**
     * 간소화 JSON 출력 (type, name, startLine, endLine, children만 포함)
     * 트리 구조 확인용
     */
    public String toSimpleJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        json.append("\"type\":\"").append(type).append("\"");
        
        if (name != null) {
            json.append(",\"name\":\"").append(escapeJson(name)).append("\"");
        }
        if (fileName != null) {
            json.append(",\"fileName\":\"").append(escapeJson(fileName)).append("\"");
        }
        if (filePath != null) {
            json.append(",\"filePath\":\"").append(escapeJson(filePath)).append("\"");
        }
        if (packageName != null) {
            json.append(",\"packageName\":\"").append(escapeJson(packageName)).append("\"");
        }
        if (moduleName != null) {
            json.append(",\"moduleName\":\"").append(escapeJson(moduleName)).append("\"");
        }
        
        json.append(",\"startLine\":").append(startLine);
        json.append(",\"endLine\":").append(endLine);
        
        json.append(",\"children\":[");
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) json.append(",");
            json.append(children.get(i).toSimpleJson());
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
