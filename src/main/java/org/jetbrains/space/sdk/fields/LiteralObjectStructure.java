package org.jetbrains.space.sdk.fields;

import java.util.Arrays;
import java.util.Map;

public class LiteralObjectStructure extends ObjectStructure {

    public LiteralObjectStructure(Map<String, DatatypeStructure> fields) {
        super(fields);
    }

    @Override
    public boolean wildcardSerializable(String fieldName, String... fieldNames) {
        if (!hasField(fieldName, fieldNames)) {
            return false;
        }
        if (fieldNames.length == 0) {
            return true;
        }
        var structure = getField(fieldName);
        String subfieldName = fieldNames[0];
        String[] subfieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        return structure.wildcardSerializable(subfieldName, subfieldNames);
    }
}
