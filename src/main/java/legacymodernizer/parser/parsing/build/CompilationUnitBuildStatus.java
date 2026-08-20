package legacymodernizer.parser.parsing.build;

/** Build-command closure for one workspace translation unit and its exact origin alias. */
public enum CompilationUnitBuildStatus {
    EXACT,
    PARTIAL,
    UNRESOLVED
}
