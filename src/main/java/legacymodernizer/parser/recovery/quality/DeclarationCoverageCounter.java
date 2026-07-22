package legacymodernizer.parser.recovery.quality;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.recovery.quality.DeclarationCoverage;

public final class DeclarationCoverageCounter {

    private static final ObjectMapper JSON = new ObjectMapper();

    private DeclarationCoverageCounter() {
    }

    public static DeclarationCoverage count(Parser parser, ParseTree tree,
                                            Set<String> declarationRules,
                                            String astJson, Set<String> emittedTypes) {
        return count(parser, tree, declarationRules, context -> true, astJson, emittedTypes);
    }

    public static DeclarationCoverage count(Parser parser, ParseTree tree,
                                            Set<String> declarationRules,
                                            Predicate<ParserRuleContext> declarationContract,
                                            String astJson, Set<String> emittedTypes) {
        Map<String, Integer> discovered = new LinkedHashMap<>();
        walkRules(parser, tree, declarationRules, declarationContract, discovered);

        Map<String, Integer> emitted = new LinkedHashMap<>();
        try {
            walkNodes(JSON.readTree(astJson), emittedTypes, emitted);
        } catch (Exception exception) {
            return new DeclarationCoverage(
                    discovered.values().stream().mapToInt(Integer::intValue).sum(),
                    0,
                    Map.copyOf(discovered),
                    Map.of(),
                    List.of("AST_JSON_UNREADABLE"));
        }

        int discoveredCount = discovered.values().stream().mapToInt(Integer::intValue).sum();
        int emittedCount = emitted.values().stream().mapToInt(Integer::intValue).sum();
        List<String> missing = new ArrayList<>();
        for (int index = emittedCount; index < discoveredCount; index++) {
            missing.add("DECLARATION_" + (index + 1));
        }
        return new DeclarationCoverage(discoveredCount, emittedCount,
                Map.copyOf(discovered), Map.copyOf(emitted), List.copyOf(missing));
    }

    private static void walkRules(Parser parser, ParseTree tree, Set<String> declarationRules,
                                  Predicate<ParserRuleContext> declarationContract,
                                  Map<String, Integer> discovered) {
        if (tree instanceof ParserRuleContext context) {
            String name = parser.getRuleNames()[context.getRuleIndex()];
            if (declarationRules.contains(name) && declarationContract.test(context)) {
                discovered.merge(name, 1, Integer::sum);
            }
        }
        for (int index = 0; index < tree.getChildCount(); index++) {
            walkRules(parser, tree.getChild(index), declarationRules,
                    declarationContract, discovered);
        }
    }

    private static void walkNodes(JsonNode node, Set<String> emittedTypes,
                                  Map<String, Integer> emitted) {
        if (node == null || !node.isObject()) {
            return;
        }
        String type = node.path("type").asText("");
        if (emittedTypes.contains(type)) {
            emitted.merge(type, 1, Integer::sum);
        }
        JsonNode children = node.path("children");
        if (children.isArray()) {
            children.forEach(child -> walkNodes(child, emittedTypes, emitted));
        }
    }
}
