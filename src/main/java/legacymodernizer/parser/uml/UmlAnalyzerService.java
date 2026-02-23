package legacymodernizer.parser.uml;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * JavaParser 기반 UML 관계 분석
 * - 상속/구현, 연관/집합/합성 분류
 * - 점수 기반 ownership, DI/setter/new 패턴 감지
 */
@Service
public class UmlAnalyzerService {

    public UmlResult analyze(Path srcRoot) throws IOException {

        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JavaParserTypeSolver(srcRoot));

        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(solver));

        JavaParser parser = new JavaParser(config);

        List<UmlRelation> relations = new ArrayList<>();

        Files.walk(srcRoot)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        ParseResult<CompilationUnit> parseResult = parser.parse(file);
                        CompilationUnit cu = parseResult.getResult().orElse(null);
                        if (cu == null) {
                            return;
                        }

                        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {

                            String owner = clazz.getFullyQualifiedName()
                                    .orElse(clazz.getNameAsString());

                            // 1. 상속 / 구현
                            clazz.getExtendedTypes().forEach(ext -> {
                                try {
                                    relations.add(new UmlRelation(
                                            owner,
                                            ext.resolve().describe(),
                                            "INHERITANCE",
                                            100,
                                            List.of("extends")
                                    ));
                                } catch (Exception ignored) {
                                }
                            });

                            clazz.getImplementedTypes().forEach(impl -> {
                                try {
                                    relations.add(new UmlRelation(
                                            owner,
                                            impl.resolve().describe(),
                                            "IMPLEMENTATION",
                                            100,
                                            List.of("implements")
                                    ));
                                } catch (Exception ignored) {
                                }
                            });

                            // 2. 필드 기반 관계 분석
                            for (FieldDeclaration field : clazz.getFields()) {
                                try {
                                    ResolvedType resolved = field.getVariables().get(0).getType().resolve();
                                    String target = resolved.describe();

                                    int score = 2;
                                    List<String> evidence = new ArrayList<>();
                                    evidence.add("field");

                                    if (field.isPrivate()) {
                                        score += 1;
                                        evidence.add("private");
                                    }

                                    if (field.isFinal()) {
                                        score += 2;
                                        evidence.add("final");
                                    }

                                    if (isCollection(resolved)) {
                                        score += 1;
                                        evidence.add("collection");
                                    }

                                    if (hasSetter(clazz, field)) {
                                        score -= 3;
                                        evidence.add("setter");
                                    }

                                    if (isInjected(field)) {
                                        score -= 2;
                                        evidence.add("injected");
                                    }

                                    relations.add(classify(owner, target, score, evidence));
                                } catch (Exception ignored) {
                                }
                            }

                            // 3. 생성자 new 패턴 → 합성
                            for (ConstructorDeclaration ctor : clazz.getConstructors()) {
                                ctor.findAll(AssignExpr.class).forEach(assign -> {
                                    try {
                                        if (assign.getValue() instanceof ObjectCreationExpr creation) {
                                            String target = creation.getType().resolve().describe();
                                            relations.add(new UmlRelation(
                                                    owner,
                                                    target,
                                                    "COMPOSITION",
                                                    10,
                                                    List.of("new-in-constructor")
                                            ));
                                        }
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        // 파일 단위 파싱/분석 실패 시 스킵
                    }
                });

        return new UmlResult(relations);
    }

    private UmlRelation classify(String from, String to, int score, List<String> evidence) {
        String type;
        if (score >= 7) {
            type = "COMPOSITION";
        } else if (score >= 3) {
            type = "AGGREGATION";
        } else {
            type = "ASSOCIATION";
        }
        return new UmlRelation(from, to, type, score, evidence);
    }

    private boolean hasSetter(ClassOrInterfaceDeclaration clazz, FieldDeclaration field) {
        String fieldName = field.getVariables().get(0).getNameAsString();
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
        return clazz.getMethodsByName(setterName).size() > 0;
    }

    private boolean isInjected(FieldDeclaration field) {
        return field.getAnnotations().stream()
                .anyMatch(a -> {
                    String name = a.getNameAsString();
                    return name.equals("Autowired") || name.equals("Inject");
                });
    }

    private boolean isCollection(ResolvedType type) {
        String name = type.describe();
        return name.contains("List")
                || name.contains("Set")
                || name.contains("Collection")
                || name.contains("Map");
    }
}
