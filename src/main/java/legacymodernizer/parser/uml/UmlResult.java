package legacymodernizer.parser.uml;

import java.util.List;

/**
 * UML 분석 결과
 */
public record UmlResult(
        List<UmlRelation> relations
) {}
