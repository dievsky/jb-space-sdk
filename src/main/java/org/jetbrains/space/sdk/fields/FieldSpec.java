package org.jetbrains.space.sdk.fields;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a field specification as passed to Space HTTP API via $fields query parameter.
 */
public class FieldSpec {

    private final @NotNull FieldSpecs nested;
    private final boolean recursive;

    public @NotNull FieldSpecs getNested() {
        return nested;
    }

    FieldSpec(@NotNull FieldSpecs nested, boolean recursive) {
        this.nested = nested;
        this.recursive = recursive;
    }

    @Override
    public @NotNull String toString() {
        if (recursive) {
            return "!";
        }
        if (nested.isWildcard()) {
            return "";
        }
        return "(" + nested.toString() + ")";
    }
}
