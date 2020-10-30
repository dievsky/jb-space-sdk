package org.jetbrains.space.sdk.fields;

import java.util.Arrays;
import java.util.Map;

public abstract class ObjectStructure implements DatatypeStructure {

    protected final Map<String, DatatypeStructure> fields;

    public ObjectStructure(Map<String, DatatypeStructure> fields) {
        this.fields = fields;
    }

    @Override
    public boolean hasField(String fieldName, String... fieldNames) {
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
    public DatatypeStructure getField(String fieldName, String... fieldNames) {
        final DatatypeStructure structure = fields.get(fieldName);
        if (fieldNames.length == 0) {
            return structure;
        }
        String subfieldName = fieldNames[0];
        String[] subfieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        return structure.getField(subfieldName, subfieldNames);
    }
}
