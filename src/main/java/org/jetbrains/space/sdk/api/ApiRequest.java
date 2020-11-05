package org.jetbrains.space.sdk.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public interface ApiRequest<T> {

  /**
   * Add a query parameter to the request, for example "id=12345678".
   *
   * @param key   Query parameter key.
   * @param value Query parameter value.
   * @return this request, following builder pattern.
   */
  @NotNull ApiRequest<T> addParameter(@NotNull String key, @NotNull String value);

  /**
   * Add a date-valued query parameter to the request, for example "since=2020-07-21".
   *
   * @param key   Query parameter key.
   * @param value Query parameter value.
   * @return this request, following builder pattern.
   */
  default @NotNull ApiRequest<T> addParameter(@NotNull String key, @NotNull LocalDate value) {
    return addParameter(key, value.format(DateTimeFormatter.ISO_DATE));
  }

  /**
   * Add a boolean-valued query parameter to the request, for example "reportPastMembers=true".
   *
   * @param key   Query parameter key.
   * @param value Query parameter value.
   * @return this request, following builder pattern.
   */
  @SuppressWarnings("unused")
  default @NotNull ApiRequest<T> addParameter(@NotNull String key, boolean value) {
    return addParameter(key, String.valueOf(value));
  }

  /**
   * Add a multi-value query parameter to the request, for example "members=id1&members=id2&members=id3".
   * <p>
   * Lists longer than 20 are implicitly split into smaller chunks and processed in separate requests.
   * The results of these requests are then concatenated.
   * <p>
   * Only one multi-value parameter per request can be specified.
   *
   * @param key    Query parameter key.
   * @param values Query parameter values.
   * @return this request, following builder pattern.
   */
  @NotNull ApiRequest<T> addParameterList(@NotNull String key, @NotNull Collection<String> values);

  /**
   * Ask to receive a specific field.
   *
   * By default, Space serializes all the immediate fields of the response object. The primitive and value fields
   * are serialized fully, while the reference fields are serialized with only their `id`. You can use this method
   * to get the embedded fields.
   *
   * For example, {@code addField("member", "location", "id")} requests `object.member.location.id` field, where
   * `object` is the object returned by the API request. If you don't ask for this field, `object.member` will
   * only have one non-null field, `object.member.id`, so accessing `object.member.location.id` will cause an NPE.
   *
   * Keen readers may notice that the above invocation is redundant. Indeed, since `id` is always serialized
   * when its parent is, {@code addField("member", "location")} would have the same effect.
   *
   * @param fieldName  The immediate field name.
   * @param fieldNames The nested fields, if any.
   * @return this request, following builder pattern.
   */
  @NotNull ApiRequest<T> addField(@NotNull String fieldName, String... fieldNames);

  /**
   * Ask to receive a specific recursively serialized field.
   *
   * Some objects have fields of the same type as the enclosing object.
   * E.g. a {@link org.jetbrains.space.sdk.datatype.TD_Location} object has a field `parent`
   * of the type {@link org.jetbrains.space.sdk.datatype.TD_Location}. Calling an ordinary {@code addField("parent"}
   * will serialize the location's parent, but the grandparent (`location.parent.parent`) will only have the `id` field,
   * and the grand-grandparent will not be serialized at all. To serialize the entire chain, use
   * {@code addRecursiveField("parent")}.
   *
   * @param fieldName  The immediate field name.
   * @param fieldNames The nested fields, if any.
   * @return this request, following builder pattern.
   */
  @NotNull ApiRequest<T> addRecursiveField(@NotNull String fieldName, String... fieldNames);

  /**
   * Execute the request and return the result.
   *
   * This method doesn't invalidate the ApiRequest object in any way. You may modify the request and
   * execute it again.
   *
   * @return the request result as an appropriate Java object.
   * @throws IOException          on network problems.
   * @throws InterruptedException if interrupted.
   */
  @NotNull T execute() throws IOException, InterruptedException;
}
