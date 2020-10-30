package org.jetbrains.space.sdk.fields;

import java.util.Map;

public class ReferenceStructure extends ObjectStructure {

    public ReferenceStructure(Map<String, DatatypeStructure> fields) {
        super(fields);
        this.fields.put("id", PRIMITIVE);
    }

    @Override
    public boolean wildcardSerializable(String fieldName, String... fieldNames) {
        return fieldNames.length == 0 && "id".equals(fieldName);
    }
}
