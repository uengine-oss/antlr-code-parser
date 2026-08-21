package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** One ordered binding entry owned by an import/include grammar statement. */
public record ImportBindingCandidate(
        String importKind,
        String targetKind,
        SourceRangeCandidate targetRange,
        List<SourceRangeCandidate> pathComponentRanges,
        SourceRangeCandidate memberRange,
        SourceRangeCandidate aliasRange,
        int relativeLevel,
        boolean wildcard,
        String locality) {

    private static final List<String> IMPORT_KINDS = List.of(
            "source_file", "computed", "type", "namespace", "static_member",
            "module", "module_member");
    private static final List<String> TARGET_KINDS = List.of(
            "quoted", "angle", "computed", "qualified");
    private static final List<String> LOCALITIES = List.of(
            "local", "system", "unspecified");

    public ImportBindingCandidate {
        if (!IMPORT_KINDS.contains(importKind)) {
            throw new IllegalArgumentException("unsupported importKind: " + importKind);
        }
        if (!TARGET_KINDS.contains(targetKind)) {
            throw new IllegalArgumentException("unsupported targetKind: " + targetKind);
        }
        if (targetRange == null) {
            throw new IllegalArgumentException("import targetRange is required");
        }
        pathComponentRanges = List.copyOf(
                pathComponentRanges == null ? List.of() : pathComponentRanges);
        if (relativeLevel < 0) {
            throw new IllegalArgumentException("relativeLevel must be non-negative");
        }
        if (!LOCALITIES.contains(locality)) {
            throw new IllegalArgumentException("unsupported import locality: " + locality);
        }
        if ("computed".equals(importKind)) {
            if (!"computed".equals(targetKind) || !pathComponentRanges.isEmpty()
                    || memberRange != null || aliasRange != null || wildcard
                    || relativeLevel != 0 || !"unspecified".equals(locality)) {
                throw new IllegalArgumentException("computed import cannot claim a resolved binding");
            }
        }
        if ("source_file".equals(importKind)) {
            if (!List.of("quoted", "angle").contains(targetKind)
                    || pathComponentRanges.size() != 1 || memberRange != null
                    || aliasRange != null || wildcard || relativeLevel != 0
                    || !("quoted".equals(targetKind) == "local".equals(locality))) {
                throw new IllegalArgumentException("invalid source-file import binding");
            }
        }
        if (List.of("type", "namespace", "static_member", "module", "module_member")
                .contains(importKind) && !"qualified".equals(targetKind)) {
            throw new IllegalArgumentException(importKind + " requires qualified targetKind");
        }
        if ("namespace".equals(importKind) && !wildcard) {
            throw new IllegalArgumentException("namespace import must be on-demand");
        }
        if ("static_member".equals(importKind) && (wildcard == (memberRange != null))) {
            throw new IllegalArgumentException("static member import needs member xor wildcard");
        }
        if ("module_member".equals(importKind) && (wildcard == (memberRange != null))) {
            throw new IllegalArgumentException("module member import needs member xor wildcard");
        }
    }
}
