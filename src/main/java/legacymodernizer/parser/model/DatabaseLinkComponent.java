package legacymodernizer.parser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** One grammar-owned identifier component of an Oracle database link. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"role", "name", "nameQuoted"})
public class DatabaseLinkComponent {
    public String role;
    public String name;
    public Boolean nameQuoted;
}
