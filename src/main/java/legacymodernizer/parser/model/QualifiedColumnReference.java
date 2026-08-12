package legacymodernizer.parser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Explicit qualifier.column spelling recognized against a visible physical object. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"rawReference", "qualifier", "name", "nameQuoted", "startLine"})
public class QualifiedColumnReference {
    public String rawReference;
    public String qualifier;
    public String name;
    public Boolean nameQuoted;
    public int startLine;
}
