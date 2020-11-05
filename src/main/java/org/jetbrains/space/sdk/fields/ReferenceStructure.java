package org.jetbrains.space.sdk.fields;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReferenceStructure extends ObjectStructure {

    public ReferenceStructure(@NotNull Map<String, DatatypeStructure> fields) {
        super(fields);
        this.fields.put("id", PRIMITIVE);
    }

    @Override
    public boolean wildcardSerializable(@NotNull String fieldName, @NotNull String... fieldNames) {
        return fieldNames.length == 0 && "id".equals(fieldName);
    }
}
