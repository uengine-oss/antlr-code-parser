package legacymodernizer.parser.parsing.languages.oracle;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import legacymodernizer.parser.antlr.CaseChangingCharStream;
import legacymodernizer.parser.antlr.plsql.PlSqlLexer;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import legacymodernizer.parser.antlr.plsql.PlSqlParserBaseListener;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/** Locates direct package-body routines using ANTLR token coordinates. */
public final class OraclePackageMemberLocator {

    public List<SourceUnit> locate(String fileSource, SourceUnit packageUnit) {
        if (fileSource == null || packageUnit == null || packageUnit.kind() != UnitKind.PACKAGE) {
            return List.of();
        }
        String packageSource = fileSource.substring(packageUnit.startOffset(), packageUnit.endOffset());
        PlSqlLexer lexer = new PlSqlLexer(new CaseChangingCharStream(CharStreams.fromString(packageSource), true));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        parser.removeErrorListeners();

        List<SourceUnit> members = new ArrayList<>();
        ParseTreeWalker.DEFAULT.walk(new PlSqlParserBaseListener() {
            @Override
            public void enterPackage_obj_body(PlSqlParser.Package_obj_bodyContext context) {
                if (context.getStart() == null || context.getStop() == null) return;
                UnitKind kind = context.function_body() != null
                        ? UnitKind.FUNCTION : UnitKind.PROCEDURE;
                String name = context.function_body() != null
                        && context.function_body().identifier() != null
                        ? context.function_body().identifier().getText()
                        : context.procedure_body() != null
                            && context.procedure_body().identifier() != null
                            ? context.procedure_body().identifier().getText() : null;
                int relativeStart = context.getStart().getStartIndex();
                int relativeEnd = context.getStop().getStopIndex() + 1;
                if (relativeStart < 0 || relativeEnd <= relativeStart
                        || relativeEnd > packageSource.length()) return;
                int absoluteStart = packageUnit.startOffset() + relativeStart;
                int absoluteEnd = packageUnit.startOffset() + relativeEnd;
                int startLine = packageUnit.startLine() + context.getStart().getLine() - 1;
                int endLine = packageUnit.startLine() + context.getStop().getLine() - 1;
                String identity = "oracle-package-member\n" + packageUnit.unitId() + "\n"
                        + kind + "\n" + absoluteStart + "\n" + absoluteEnd + "\n"
                        + fileSource.substring(absoluteStart, absoluteEnd);
                members.add(new SourceUnit(
                        Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                        kind, name, packageUnit.unitId(), absoluteStart, absoluteEnd,
                        startLine, endLine, members.size(), "EXACT"));
            }
        }, parser.sql_script());
        members.sort(Comparator.comparingInt(SourceUnit::startOffset));
        for (int index = 1; index < members.size(); index++) {
            if (members.get(index - 1).endOffset() > members.get(index).startOffset()) {
                return List.of();
            }
        }
        return List.copyOf(members);
    }
}
