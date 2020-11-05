package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.LiteralObjectStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchResponse<T> {

    public static @NotNull DatatypeStructure structure(@NotNull DatatypeStructure elementStructure) {
        Map<String, DatatypeStructure> fields = new HashMap<>();
        fields.put("next", DatatypeStructure.PRIMITIVE);
        fields.put("totalCount", DatatypeStructure.PRIMITIVE);
        fields.put("data", elementStructure);
        return new LiteralObjectStructure(fields);
    }

    public final @NotNull String next;
    public final int totalCount;
    public final @NotNull List<T> data;

    private BatchResponse(@NotNull String next, int totalCount, @NotNull List<T> data) {
        this.next = next;
        this.totalCount = totalCount;
        this.data = data;
    }
}
