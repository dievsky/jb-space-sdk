package org.jetbrains.space.sdk.fields;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public abstract class ObjectStructure implements DatatypeStructure {

    protected final @NotNull Map<String, DatatypeStructure> fields;

    public ObjectStructure(@NotNull Map<String, DatatypeStructure> fields) {
        this.fields = fields;
    }

    @Override
    public boolean hasField(@NotNull String fieldName, @NotNull String... fieldNames) {
        if (!fields.containsKey(fieldName)) {
            return false;
        }
        if (fieldNames.length == 0) {
            return true;
        }
        String subfieldName = fieldNames[0];
        String[] subfieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        return fields.get(fieldName).hasField(subfieldName, subfieldNames);
    }

    @Override
    public @NotNull DatatypeStructure getField(@NotNull String fieldName, @NotNull String... fieldNames) {
        final DatatypeStructure structure = fields.get(fieldName);
        if (fieldNames.length == 0) {
            return structure;
        }
        String subfieldName = fieldNames[0];
        String[] subfieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        return structure.getField(subfieldName, subfieldNames);
    }
}
