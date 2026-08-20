package legacymodernizer.parser.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Exact grammar-owned physical data-object reference attached to a DML AST node. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "rawReference", "catalog", "schema", "name", "catalogQuoted", "schemaQuoted", "nameQuoted",
    "databaseLink", "databaseLinkComponents", "alias", "access", "startLine"
})
public class DataObjectReference {
    public String rawReference;
    public String catalog;
    public String schema;
    public String name;
    public Boolean catalogQuoted;
    public Boolean schemaQuoted;
    public Boolean nameQuoted;
    public String databaseLink;
    public ArrayList<DatabaseLinkComponent> databaseLinkComponents;
    public String alias;
    public String access;
    public int startLine;
}
