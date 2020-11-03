package org.jetbrains.space.sdk;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.space.sdk.datatype.*;
import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.FieldSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Describes a Space service that allows to make API calls.
 *
 * The API requests correspond to the getter-style methods.
 * For example, the getHolidays() method queries the "/api/http/public-holidays/holidays" API endpoint.
 *
 * Required request parameters correspond to the method parameters.
 * For example, the getProfileHolidays(...) method accepts three parameters,
 * because the "/api/http/public-holidays/holidays/profile-holidays" endpoint has three required parameters.
 *
 * Optional parameters can be supplied to the request using addParameter or addParameterList.
 * For example, to add an optional parameter "since" to an "/api/http/team-directory/member-locations" query,
 * use getMemberLocations().addParameter("since", date).
 * These methods follow the builder pattern and can be chained.
 *
 * To control the output fields, use addField and addRecursiveField methods.
 *
 * Our library handles the pagination and chunking of parameter lists internally.
 * It also implicitly requests and refreshes the OAuth token as needed.
 *
 * If your favorite endpoint is not implemented yet, you can construct thr request yourself with get* methods!
 * There are three types of Space API GET queries. Queries can return:
 * - a single object. Constructed using `get()`.
 * - a list of objects. Constructed using `getList()`.
 * - a batch of objects (a part of the list). Constructed using `getBatch()`.
 */
public class SpaceService {

    private final String domain;
    private final String serviceId;
    private final String serviceSecret;
    private final OAuthToken oauth;
    private final HttpClient httpClient;
    private final Logger logger;

    private static final int SERVER_ERROR_RETRIES = 2;

    /**
     * @param domain the domain name of the Space server, e.g. "jetbrains.team".
     * @param serviceId The service ID.
     * @param serviceSecret The service secret.
     */
    public SpaceService(String domain, String serviceId, String serviceSecret) {
        this.domain = domain;
        this.serviceId = serviceId;
        this.serviceSecret = serviceSecret;
        oauth = new OAuthToken();
        httpClient = HttpClient.newBuilder().build();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    private HttpRequest.Builder stringRequest(@NotNull String api, @NotNull String authorization,
                                              @NotNull String method, @NotNull String payload) {
        return HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.ofString(payload))
                .header("Authorization", authorization)
                .header("Content-Type", "text/plain")
                .header("Accept", "application/json")
                .uri(URI.create("https://" + domain + api));
    }

    private HttpRequest.Builder keyValueRequest(@NotNull String api, @NotNull String authorization,
                                                @NotNull String method, Map<String, Object> payload) {
        var res = stringRequest(api, authorization, method, SpaceQueryParameters.toPostBody(payload));
        if ("GET".equals(method)) {
            // we have to encode the body in the URL instead
            res.method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create("https://" + domain + api + SpaceQueryParameters.toQueryParameters(payload)));
        } else {
            res.setHeader("Content-Type", "application/json");
        }
        return res;
    }

    private JsonElement rawQuery(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = null;
        for (int i = 0; i <= SERVER_ERROR_RETRIES; i++) {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                if (statusCode >= 500 && statusCode < 600) {
                    // server-side error, wait and retry
                    Thread.sleep(200);
                    continue;
                } else {
                    break;
                }
            }
            return JsonParser.parseString(response.body());
        }
        throw new RuntimeException(String.valueOf(response));
    }

    /**
     * The request to get the public holidays.
     *
     * Accepts the following optional parameters:
     * - "startDate", inclusive, LocalDate
     * - "endDate", inclusive, LocalDate
     * - "location", the ID of a location, String. Note that the result will also include the holidays
     *   for all the parent locations.
     */
    public ApiRequest<List<PublicHoliday>> getHolidays() {
        return getBatch("/api/http/public-holidays/holidays", PublicHoliday.class);
    }

    /**
     * The request to get the public holidays for a specific member.
     *
     * @param memberId the ID of the member, String.
     * @param since start date, inclusive, LocalDate.
     * @param till end date, inclusive, LocalDate.
     */
    @SuppressWarnings("unused")
    public ApiRequest<List<PublicHoliday>> getProfileHolidays(String memberId, LocalDate since, LocalDate till) {
        return getList("/api/http/public-holidays/holidays/profile-holidays", PublicHoliday.class)
                .addParameter("startDate", since).addParameter("endDate", till).addParameter("profile", memberId);
    }

    /**
     * The request to get the absence records.
     *
     * Accepts the following optional filtering parameters:
     * - "member", the ID of a specific member, String.
     * - "members", the list of IDs of specific members, List<String>.
     * - "location", the ID of a location, String.
     * - "team", the ID of a team, String.
     * - "since", start date, inclusive, LocalDate.
     * - "till", end date, inclusive, LocalDate.
     * - "reason", the ID of a specific absence reason, String.
     *
     * @param viewMode One of "All", "WithAccessibleReasonUnapproved", or "WithAccessibleReasonAll"
     */
    public ApiRequest<List<AbsenceRecord>> getAbsences(String viewMode) {
        return getBatch("/api/http/absences", AbsenceRecord.class).addParameter("viewMode", viewMode);
    }

    /**
     * The request to get the member profiles.
     *
     * Accepts the following optional filtering parameters:
     * "query", a string query, String.
     * "reportPastMembers", whether to include the members who are no longer active, boolean.
     *
     */
    @SuppressWarnings("unused")
    public ApiRequest<List<TD_MemberProfile>> getProfiles() {
        return getBatch("/api/http/team-directory/profiles", TD_MemberProfile.class);
    }

    public ApiRequest<List<TD_MemberLocation>> getMemberLocations() {
        return getBatch("/api/http/team-directory/member-locations", TD_MemberLocation.class);
    }

    @SuppressWarnings("unused")
    public ApiRequest<List<TD_WorkingDays>> getWorkingDays(String id) {
        return getBatch("/api/http/team-directory/profiles/id:" + id +  "/working-days", TD_WorkingDays.class);
    }

    public ApiRequest<List<TD_ProfileWorkingDays>> getWorkingDays() {
        return getBatch("/api/http/team-directory/profiles/working-days", TD_ProfileWorkingDays.class);
    }

    @SuppressWarnings("unused")
    public ApiRequest<List<BusinessEntity>> getBusinessEntities() {
        return getList("/api/http/hrm/business-entities", BusinessEntity.class);
    }

    public ApiRequest<List<BusinessEntityRelation>> getBusinessEntityRelations() {
        return getBatch("/api/http/hrm/business-entities/relations", BusinessEntityRelation.class);
    }

    @SuppressWarnings("unused")
    public ApiRequest<List<BusinessEntityRelation>> getBusinessEntityRelations(String memberId) {
        return getList("/api/http/hrm/business-entities/relations/" + memberId, BusinessEntityRelation.class);
    }

    /**
     * The request to get an object from an arbitrary endpoint.
     *
     * @param endpoint the API endpoint, e.g. "/api/http/team-directory/profiles/abcdefghijkl"
     * @param objectType The expected response type, e.g. `TD_MemberProfile.class`.
     * @param <T> The response type.
     */
    @SuppressWarnings("unused")
    public <T> ApiRequest<T> get(String endpoint, Class<T> objectType) {
        return new ObjectApiRequest<>(endpoint, "GET", objectType,
                DatatypeStructureDiscovery.structure(objectType));
    }

    /**
     * The request to get a (non-batched) list from an arbitrary endpoint.
     *
     * @param endpoint the API endpoint, e.g. "/api/http/hrm/business-entities".
     * @param elementType The expected list element type, e.g. `BusinessEntity.class`.
     * @param <T> The list element type.
     */
    public <T> ApiRequest<List<T>> getList(String endpoint, Class<T> elementType) {
        return new ObjectApiRequest<>(endpoint, "GET",
                TypeToken.getParameterized(List.class, elementType).getType(),
                DatatypeStructureDiscovery.structure(elementType));
    }

    /**
     * The request to get a batched list from an arbitrary endpoint.
     *
     * @param endpoint the API endpoint, e.g. "/api/http/absences".
     * @param elementType The expected list element type, e.g. `AbsenceRecord.class`.
     * @param <T> The list element type.
     */
    public <T> ApiRequest<List<T>> getBatch(String endpoint, Class<T> elementType) {
        return new BatchApiRequest<>(endpoint, "GET", elementType);
    }

    private class OAuthToken {

        private @Nullable String token = null;
        private @Nullable LocalDateTime expires = null;

        private boolean expired() {
            return expires == null || LocalDateTime.now().compareTo(expires) > 0;
        }

        private void refreshIfNeeded() throws IOException, InterruptedException {
            if (expired()) refresh();
        }

        private void refresh() throws IOException, InterruptedException {
            var requestBuilder = stringRequest("/oauth/token",
                    "Basic " + Base64.getEncoder().encodeToString(
                            (serviceId + ":" + serviceSecret).getBytes(StandardCharsets.UTF_8)),
                    "POST", "grant_type=client_credentials&scope=**");
            requestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            JsonElement response = rawQuery(requestBuilder.build());
            token = response.getAsJsonObject().get("access_token").getAsString();
            expires = LocalDateTime.now().plus(response.getAsJsonObject().get("expires_in").getAsInt(),
                    ChronoUnit.SECONDS);
        }
    }

    private static final TypeAdapter<LocalDate> LOCAL_DATE_TYPE_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value.format(DateTimeFormatter.ISO_DATE));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek().equals(JsonToken.STRING)) {
                return LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_DATE);
            }
            in.beginObject();
            LocalDate res = null;
            while (!in.peek().equals(JsonToken.END_OBJECT)) {
                String name = in.nextName();
                if (name.equals("iso")) {
                    res = LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_DATE);
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return res;
        }
    };

    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_TYPE_ADAPTER.nullSafe())
            .registerTypeAdapterFactory(CFValue.ADAPTER_FACTORY)
            .create();


    public interface ApiRequest<T> {

        /**
         * Add a query parameter to the request, for example "id=12345678".
         *
         * @param key Query parameter key.
         * @param value Query parameter value.
         * @return this request, following builder pattern.
         */
        ApiRequest<T> addParameter(String key, String value);

        /**
         * Add a date-valued query parameter to the request, for example "since=2020-07-21".
         *
         * @param key Query parameter key.
         * @param value Query parameter value.
         * @return this request, following builder pattern.
         */
        default ApiRequest<T> addParameter(String key, LocalDate value) {
            return addParameter(key, value.format(DateTimeFormatter.ISO_DATE));
        }

        /**
         * Add a boolean-valued query parameter to the request, for example "reportPastMembers=true".
         *
         * @param key Query parameter key.
         * @param value Query parameter value.
         * @return this request, following builder pattern.
         */
        @SuppressWarnings("unused")
        default ApiRequest<T> addParameter(String key, boolean value) {
            return addParameter(key, String.valueOf(value));
        }

        /**
         * Add a multi-value query parameter to the request, for example "members=id1&members=id2&members=id3".
         *
         * Lists longer than 20 are implicitly split into smaller chunks and processed in separate requests.
         * The results of these requests are then concatenated.
         *
         * Only one multi-value parameter per request can be specified.
         *
         * @param key Query parameter key.
         * @param values Query parameter values.
         * @return this request, following builder pattern.
         */
        ApiRequest<T> addParameterList(String key, Collection<String> values);

        /**
         * Ask to receive a specific field.
         *
         * By default, Space serializes all the immediate fields of the response object. The primitive and value fields
         * are serialized fully, while the reference fields are serialized with only their `id`. You can use this method
         * to get the embedded fields.
         *
         * For example, `addField("member", "location", "id")` requests `object.member.location.id` field, where
         * `object` is the object returned by the API request. If you don't ask for this field, `object.member` will
         * only have one non-null field, `object.member.id`, so accessing `object.member.location.id` will cause an NPE.
         *
         * Keen readers may notice that the above invocation is redundant. Indeed, since `id` is always serialized
         * when its parent is, `addField("member", "location")` would have the same effect.
         *
         * @param fieldName The immediate field name.
         * @param fieldNames The nested fields, if any.
         * @return this request, following builder pattern.
         */
        ApiRequest<T> addField(String fieldName, String... fieldNames);

        /**
         * Ask to receive a specific recursively serialized field.
         *
         * Some objects have fields of the same type as the enclosing object. E.g. a `TD_Location` object has a field
         * `parent` of the type TD_Location. Calling an ordinary `addField("parent")` will serialize
         * the location's parent, but the grandparent (`location.parent.parent`) will only have the `id` field,
         * and the grand-grandparent will not be serialized at all. To serialize the entire chain, use
         * `addRecursiveField("parent")`.
         *
         * @param fieldName The immediate field name.
         * @param fieldNames The nested fields, if any.
         * @return this request, following builder pattern.
         */
        ApiRequest<T> addRecursiveField(String fieldName, String... fieldNames);

        /**
         * Execute the request and return the result.
         *
         * `execute` doesn't invalidate the ApiRequest object in any way. You may modify the request and
         * execute it again.
         *
         * @return the request result as an appropriate Java object.
         * @throws IOException on network problems.
         * @throws InterruptedException if interrupted.
         */
        T execute() throws IOException, InterruptedException;
    }


    private class ObjectApiRequest<T> implements ApiRequest<T> {

        private final String api;
        private final String method;
        private final Type type;
        private final Map<String, Object> parameterMap = new HashMap<>();
        private final FieldSpecs specs;

        private ObjectApiRequest(String api, String method, Type type, DatatypeStructure structure) {
            this.api = api;
            this.method = method;
            this.type = type;
            specs = new FieldSpecs(true, new HashMap<>(), structure);
        }

        @Override
        public ObjectApiRequest<T> addParameter(String key, String value) {
            if (key.startsWith("$")) {
                throw new IllegalArgumentException("special parameter " + key + " can't be set directly");
            }
            return doAddParameter(key, value);
        }

        ObjectApiRequest<T> doAddParameter(String key, Object value) {
            parameterMap.put(key, value);
            return this;
        }

        @Override
        public ObjectApiRequest<T> addField(String fieldName, String... fieldNames) {
            specs.addField(fieldName, fieldNames);
            return this;
        }

        @Override
        public ObjectApiRequest<T> addRecursiveField(String fieldName, String... fieldNames) {
            specs.addRecursiveField(fieldName, fieldNames);
            return this;
        }

        @Override
        public ApiRequest<T> addParameterList(String key, Collection<String> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T execute() throws IOException, InterruptedException {
            long start = System.currentTimeMillis();
            oauth.refreshIfNeeded();
            doAddParameter("$fields", specs.toString());
            final HttpRequest request = keyValueRequest(api, "Bearer " + oauth.token, method, parameterMap)
                    .build();
            final T res = GSON.fromJson(rawQuery(request), type);
            logger.debug("Queried {} in {} ms", request.uri(), System.currentTimeMillis() - start);
            return res;
        }
    }

    private class BatchApiRequest<T> implements ApiRequest<List<T>> {

        private final static int CHUNK_SIZE = 20;

        protected String multiparameterKey = null;
        protected List<String> multiparameterValues = null;

        protected final ObjectApiRequest<BatchResponse<T>> request;

        private BatchApiRequest(String api, String method, Type elementType) {
            Type batchType = TypeToken.getParameterized(BatchResponse.class, elementType).getType();
            request = new ObjectApiRequest<>(api, method, batchType,
                    BatchResponse.structure(DatatypeStructureDiscovery.structure(elementType)));
        }

        @Override
        public BatchApiRequest<T> addParameter(String key, String value) {
            request.addParameter(key, value);
            return this;
        }

        @Override
        public BatchApiRequest<T> addField(String fieldName, String... fieldNames) {
            request.addField("data", concatStrings(fieldName, fieldNames));
            return this;
        }

        @Override
        public BatchApiRequest<T> addRecursiveField(String fieldName, String... fieldNames) {
            request.addRecursiveField("data", concatStrings(fieldName, fieldNames));
            return this;
        }

        @Override
        public BatchApiRequest<T> addParameterList(String key, Collection<String> values) {
            if (multiparameterKey != null) {
                throw new IllegalStateException("only one multi-value parameter can be supplied");
            }
            multiparameterKey = key;
            multiparameterValues = new ArrayList<>(values);
            return this;
        }


        private List<T> doExecute() throws IOException, InterruptedException {
            BatchResponse<T> batchResponse = request.execute();
            var res = new ArrayList<>(batchResponse.data);
            String next = "!" + batchResponse.next;
            while (next != null && !next.equals(batchResponse.next) && res.size() != batchResponse.totalCount) {
                next = batchResponse.next;
                request.doAddParameter("$skip", next);
                batchResponse = request.execute();
                res.addAll(batchResponse.data);
            }
            return res;
        }

        @Override
        public List<T> execute() throws IOException, InterruptedException {
            if (multiparameterKey == null) {
                return doExecute();
            }

            var res = new ArrayList<T>();

            // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/chunked.html :((

            int chunkCount = (multiparameterValues.size() - 1) / CHUNK_SIZE + 1;

            for (int i = 0; i < chunkCount; i++) {
                List<String> chunk = multiparameterValues.subList(CHUNK_SIZE * i,
                        Math.min(multiparameterValues.size(), CHUNK_SIZE * (i + 1)));
                request.doAddParameter(multiparameterKey, chunk);
                res.addAll(doExecute());
            }

            return res;
        }

        private String[] concatStrings(String v, String[] a) {
            String[] fields = new String[a.length + 1];
            fields[0] = v;
            System.arraycopy(a, 0, fields, 1, a.length);
            return fields;
        }
    }

}
