package legacymodernizer.parser.parsing.build;

/** Capability of one compilation-database command object before a trace provider runs. */
public enum CompilationCommandCapability {
    /** Exact argv can be passed to a provider without invoking a shell parser. */
    ARGUMENT_VECTOR,
    /** Shell text is retained for evidence, but cannot authorize an exact trace. */
    SHELL_COMMAND_ONLY,
    /** Required command-object fields or inputs are missing. */
    UNRESOLVED
}
