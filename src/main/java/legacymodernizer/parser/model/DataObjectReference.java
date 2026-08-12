package legacymodernizer.parser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Exact grammar-owned physical data-object reference attached to a DML AST node. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "rawReference", "schema", "name", "schemaQuoted", "nameQuoted",
    "databaseLink", "alias", "access", "startLine"
})
public class DataObjectReference {
    public String rawReference;
    public String schema;
    public String name;
    public Boolean schemaQuoted;
    public Boolean nameQuoted;
    public String databaseLink;
    public String alias;
    public String access;
    public int startLine;
}
