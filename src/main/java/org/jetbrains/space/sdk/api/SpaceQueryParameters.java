package org.jetbrains.space.sdk.api;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SpaceQueryParameters {

    private SpaceQueryParameters() {
    }

    @NotNull
    public static String toPostBody(Map<String, Object> parameterMap) {
        if (parameterMap.isEmpty()) {
            return "";
        }
        return SpaceService.GSON.toJson(parameterMap);
    }

    @NotNull
    public static String toQueryParameters(Map<String, Object> parameterMap) {
        if (parameterMap.isEmpty()) {
            return "";
        }
        var builder = new StringBuilder().append("?");
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof List<?>) {
                for (Object o : (List<?>) value) {
                    appendKeyValue(builder, entry.getKey(), o.toString());
                }
            } else {
                appendKeyValue(builder, entry.getKey(), value.toString());
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1); // the last &
        }
        return builder.toString();
    }

    private static void appendKeyValue(StringBuilder builder, String key, String value) {
        builder.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&");
    }
}
