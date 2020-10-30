package org.jetbrains.space.sdk.datatype;

import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.LiteralObjectStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchResponse<T> {

    public static DatatypeStructure structure(DatatypeStructure elementStructure) {
        Map<String, DatatypeStructure> fields = new HashMap<>();
        fields.put("next", DatatypeStructure.PRIMITIVE);
        fields.put("totalCount", DatatypeStructure.PRIMITIVE);
        fields.put("data", elementStructure);
        return new LiteralObjectStructure(fields);
    }

    public final String next;
    public final Integer totalCount;
    public final List<T> data;

    private BatchResponse(String next, Integer totalCount, List<T> data) {
        this.next = next;
        this.totalCount = totalCount;
        this.data = data;
    }
}
