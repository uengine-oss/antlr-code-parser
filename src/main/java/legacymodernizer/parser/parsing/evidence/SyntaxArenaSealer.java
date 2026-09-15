package legacymodernizer.parser.parsing.evidence;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HexFormat;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** One grammar tree per source, using the existing parse and code-point coordinates. */
public final class SyntaxArenaSealer {
    private static final ObjectMapper JSON = new ObjectMapper();

    private SyntaxArenaSealer() {}

    public static String attach(String astJson, Parser parser, ParserRuleContext root,
                                int sourceLength, String parseStatus) throws Exception {
        ObjectNode document = (ObjectNode) JSON.readTree(astJson);
        ObjectNode evidence = (ObjectNode) document.required("evidence");
        ObjectNode arena = evidence.putObject("syntaxArena");
        arena.put("schema", "antlr-syntax-arena/v1");
        if (!"exact".equals(parseStatus)) {
            arena.put("status", "unavailable");
            arena.put("reason", "parser_recovery_requires_source_mapping");
            return JSON.writeValueAsString(document);
        }
        arena.put("status", "exact");
        arena.put("grammarFingerprint", fingerprint(parser));
        arena.put("root", 0);
        ArrayNode nodes = arena.putArray("nodes");
        ArrayDeque<Pending> pending = new ArrayDeque<>();
        pending.add(new Pending(root, null, 0, sourceLength));
        while (!pending.isEmpty()) {
            Pending item = pending.removeFirst();
            ParseTree tree = item.tree();
            if (tree instanceof TerminalNode terminal && terminal.getSymbol().getType() == Token.EOF) continue;
            int id = nodes.size();
            if (item.parentChildren() != null) item.parentChildren().add(id);
            ObjectNode node = nodes.addObject();
            Token start, stop;
            if (tree instanceof ParserRuleContext context) {
                node.put("kind", "rule");
                node.put("name", parser.getRuleNames()[context.getRuleIndex()]);
                start = context.getStart(); stop = context.getStop();
            } else if (tree instanceof TerminalNode terminal) {
                node.put("kind", "token");
                start = stop = terminal.getSymbol();
                node.put("name", parser.getVocabulary().getDisplayName(start.getType()));
            } else {
                throw new IllegalArgumentException("Unsupported ANTLR tree node");
            }
            int offset = start.getStartIndex();
            // Epsilon rules consume no token: ANTLR may leave stop null at EOF,
            // or point stop at the token preceding start.
            boolean epsilon = tree instanceof ParserRuleContext
                    && ((stop == null && start.getType() == Token.EOF)
                        || (stop != null && stop.getTokenIndex() < start.getTokenIndex()));
            if (stop == null && !epsilon) throw new IllegalArgumentException("Syntax rule has no stop token");
            // Empty optional rules can start at the next token after trailing whitespace,
            // beyond the parent's final consumed token. Anchor absence inside its parent.
            if (epsilon) offset = Math.max(item.parentOffset(), Math.min(offset, item.parentEnd()));
            int end = epsilon ? offset : stop.getType() == Token.EOF ? sourceLength : stop.getStopIndex() + 1;
            if (offset < 0 || end < offset || end > sourceLength) {
                throw new IllegalArgumentException("Invalid syntax arena source range");
            }
            ObjectNode range = node.putObject("range");
            range.put("charOffset", offset); range.put("charLength", end - offset);
            ArrayNode children = node.putArray("children");
            for (int i = 0; i < tree.getChildCount(); i++) pending.addLast(new Pending(tree.getChild(i), children, offset, end));
        }
        return JSON.writeValueAsString(document);
    }

    private record Pending(ParseTree tree, ArrayNode parentChildren, int parentOffset, int parentEnd) {}

    private static String fingerprint(Parser parser) throws NoSuchAlgorithmException {
        Lexer lexer = (Lexer) parser.getTokenStream().getTokenSource();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String automaton : new String[] {parser.getSerializedATN(), lexer.getSerializedATN()}) {
            ByteBuffer bytes = ByteBuffer.allocate(4 + 2 * automaton.length());
            bytes.putInt(automaton.length());
            for (int i = 0; i < automaton.length(); i++) bytes.putChar(automaton.charAt(i));
            digest.update(bytes.array());
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
