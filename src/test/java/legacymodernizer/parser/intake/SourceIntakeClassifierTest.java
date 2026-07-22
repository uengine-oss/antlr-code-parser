package legacymodernizer.parser.intake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.intake.SourceIntakeClassifier.Kind;

/**
 * SourceIntakeClassifier 단위 테스트 — 내용 기반 표 정의(DDL) vs 소스 분류(헌법 III).
 *
 * <p>핵심: 프로시저/함수가 든 .sql 은 절대 DDL 로 분류되지 않는다(analyzer sqlglot 크래시 차단).
 */
class SourceIntakeClassifierTest {

    private final SourceIntakeClassifier classifier = new SourceIntakeClassifier();

    @Test
    void createTable_isDdl() {
        assertEquals(Kind.DDL, classifier.classify("t.sql",
                "CREATE TABLE emp (id NUMBER, name VARCHAR2(50));"));
    }

    @Test
    void createViewIndexSequence_isDdl() {
        assertEquals(Kind.DDL, classifier.classify("v.sql", "CREATE VIEW v AS SELECT 1 FROM dual;"));
        assertEquals(Kind.DDL, classifier.classify("i.sql", "CREATE INDEX ix ON emp (id);"));
        assertEquals(Kind.DDL, classifier.classify("s.sql", "CREATE SEQUENCE seq START WITH 1;"));
    }

    @Test
    void createFunction_isSource() {
        // rwis 크래시 케이스 재현 — 오라클 table-function.
        assertEquals(Kind.SOURCE, classifier.classify("FN_X.sql",
                "CREATE OR REPLACE FUNCTION fn_x (p NUMBER)\nRETURN TABLE IS\nBEGIN\n  RETURN;\nEND;"));
    }

    @Test
    void createProcedureAndPackageAndTrigger_isSource() {
        assertEquals(Kind.SOURCE, classifier.classify("p.sql", "CREATE OR REPLACE PROCEDURE p IS BEGIN NULL; END;"));
        assertEquals(Kind.SOURCE, classifier.classify("pkg.sql", "CREATE OR REPLACE PACKAGE BODY pkg IS END;"));
        assertEquals(Kind.SOURCE, classifier.classify("trg.sql", "CREATE OR REPLACE TRIGGER trg BEFORE INSERT ON emp BEGIN NULL; END;"));
    }

    @Test
    void mixedTableAndProcedure_isSource() {
        // 표+프로시저 혼합 → 프로시저 보호 위해 SOURCE (DDL 파서에 프로시저가 닿지 않게).
        assertEquals(Kind.SOURCE, classifier.classify("mix.sql",
                "CREATE TABLE t (id NUMBER);\nCREATE OR REPLACE PROCEDURE p IS BEGIN NULL; END;"));
    }

    @Test
    void nonSqlExtension_isSource() {
        assertEquals(Kind.SOURCE, classifier.classify("Foo.java", "class Foo {}"));
        assertEquals(Kind.SOURCE, classifier.classify("x.py", "def f(): pass"));
    }

    @Test
    void nullOrEmptyOrNoDeclaration_isSource() {
        assertEquals(Kind.SOURCE, classifier.classify("a.sql", null));
        assertEquals(Kind.SOURCE, classifier.classify(null, "CREATE TABLE t (id NUMBER);"));
        assertEquals(Kind.SOURCE, classifier.classify("a.sql", "SELECT * FROM emp;"));
    }

    @Test
    void caseAndLeadingWhitespaceInsensitive() {
        assertEquals(Kind.DDL, classifier.classify("t.sql", "\n   create table t (id number);"));
        assertEquals(Kind.SOURCE, classifier.classify("p.sql", "   create or replace procedure p is begin null; end;"));
    }
}
