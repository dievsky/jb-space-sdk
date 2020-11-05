package org.jetbrains.space.sdk.fields;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents field specifications as passed to Space HTTP API via $fields query parameter.
 *
 */
public class FieldSpecs {

    /**
     * Does the query include a wildcard character? Wildcard causes all immediate fields to be serialized.
     * However, fields that are references are only serialized with their "id" field.
     */
    private final boolean wildcard;

    /**
     * Named fields from the query. Named fields each have their own FieldSpec specification, which defaults to "*".
     */
    private final @NotNull Map<String, FieldSpec> specs;

    /**
     * The full structure of the corresponding API object, describing all possible fields. Used to check
     * the correctness of the "addField" requests.
     */
    private final @NotNull DatatypeStructure structure;

    public FieldSpecs(boolean wildcard, @NotNull Map<String, FieldSpec> specs, @NotNull DatatypeStructure structure) {
        this.wildcard = wildcard;
        this.specs = specs;
        this.structure = structure;
    }

    /**
     * @return whether the specifications are equivalent to "*".
     */
    public boolean isWildcard() {
        return wildcard && specs.isEmpty();
    }

    @Override
    public @NotNull String toString() {
        var res = new ArrayList<String>();
        if (wildcard) {
            res.add("*");
        }
        for (var entry : specs.entrySet()) {
            res.add(entry.getKey() + entry.getValue().toString());
        }
        return String.join(",", res);
    }


    /**
     * @param fieldName the immediate field name
     * @param fieldNames the nested field sequence, if any
     * @return whether the field will be serialized under the current specifications.
     */
    @SuppressWarnings("unused")
    public boolean fieldWillBeSerialized(@NotNull String fieldName, @NotNull String... fieldNames) {
        if (!structure.hasField(fieldName, fieldNames)) {
            return false;
        }
        if (specs.containsKey(fieldName)) {
            var fieldSpec = specs.get(fieldName);

            if (fieldNames.length == 0) {
                return true;
            }
            String subfield = fieldNames[0];
            String[] subfields = new String[fieldNames.length - 1];
            System.arraycopy(fieldNames, 1, subfields, 0, subfields.length);
            return fieldSpec.getNested().fieldWillBeSerialized(subfield, subfields);
        }
        // no named specs found, check wildcard
        if (wildcard) {
            if (fieldNames.length == 0) {
                return true; // wildcard serializes all immediate subfields
            }
            String subfield = fieldNames[0];
            String[] subfields = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
            return structure.getField(fieldName).wildcardSerializable(subfield, subfields);
        }
        return false;
    }

    private void addField(boolean recursive, @NotNull String fieldName, @NotNull String... fieldNames) {
        if (!structure.hasField(fieldName, fieldNames)) {
            throw new NoSuchElementException();
        }
        FieldSpec fieldSpec;
        if (specs.containsKey(fieldName)) {
            fieldSpec = specs.get(fieldName);
        } else {
            var fieldStructure = structure.getField(fieldName);
            fieldSpec = new FieldSpec(new FieldSpecs(true, new HashMap<>(), fieldStructure),
                    recursive && fieldNames.length == 0);
            specs.put(fieldName, fieldSpec);
        }
        if (fieldNames.length != 0) {
            String subfield = fieldNames[0];
            String[] subfields = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
            fieldSpec.getNested().addField(recursive, subfield, subfields);
        }
    }

    /**
     * Add a field to the current specifications.
     * @param fieldName  the immediate field name
     * @param fieldNames the nested field sequence, if any
     */
    public void addField(@NotNull String fieldName, @NotNull String... fieldNames) {
        addField(false, fieldName, fieldNames);
    }

    /**
     * Add a recursive field to the current specifications.
     * @param fieldName  the immediate field name
     * @param fieldNames the nested field sequence, if any
     */
    public void addRecursiveField(@NotNull String fieldName, @NotNull String... fieldNames) {
        addField(true, fieldName, fieldNames);
    }
}
