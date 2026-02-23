package legacymodernizer.parser.uml;

import java.util.List;

/**
 * UML 관계 (상속/구현/연관/집합/합성)
 */
public record UmlRelation(
        String from,
        String to,
        String type,
        int score,
        List<String> evidence
) {}
