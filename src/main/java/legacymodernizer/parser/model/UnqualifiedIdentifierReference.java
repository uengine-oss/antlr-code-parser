package legacymodernizer.parser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Grammar-owned unqualified SQL identifier occurrence; table ownership remains downstream. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"rawReference", "name", "nameQuoted", "startLine"})
public class UnqualifiedIdentifierReference {
    public String rawReference;
    public String name;
    public Boolean nameQuoted;
    public int startLine;
}
