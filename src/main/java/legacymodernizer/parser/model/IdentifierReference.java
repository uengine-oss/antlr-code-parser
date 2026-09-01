package legacymodernizer.parser.model;

/** One lexer-confirmed identifier occurrence owned by an AST node range. */
public record IdentifierReference(String name, int line) {
    public IdentifierReference {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("identifier name must be non-blank");
        }
        if (line <= 0) {
            throw new IllegalArgumentException("identifier line must be positive");
        }
    }
}
