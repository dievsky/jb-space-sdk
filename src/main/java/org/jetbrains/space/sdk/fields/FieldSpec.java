package org.jetbrains.space.sdk.fields;

/**
 * Represents a field specification as passed to Space HTTP API via $fields query parameter.
 */
public class FieldSpec {

    private final FieldSpecs nested;
    private final boolean recursive;

    public FieldSpecs getNested() {
        return nested;
    }

    FieldSpec(FieldSpecs nested, boolean recursive) {
        this.nested = nested;
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        if (recursive) {
            return "!";
        }
        if (nested.isWildcard()) {
            return "";
        }
        return "(" + nested.toString() + ")";
    }
}
