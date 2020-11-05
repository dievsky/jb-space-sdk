package org.jetbrains.space.sdk.api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.space.sdk.datatype.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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

    private final @NotNull String domain;
    private final @NotNull String serviceId;
    private final @NotNull String serviceSecret;
    private final @NotNull OAuthToken oauth;
    private final @NotNull HttpClient httpClient;

    private static final int SERVER_ERROR_RETRIES = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceService.class);

    /**
     * @param domain the domain name of the Space server, e.g. "jetbrains.team".
     * @param serviceId The service ID.
     * @param serviceSecret The service secret.
     */
    public SpaceService(@NotNull String domain, @NotNull String serviceId, @NotNull String serviceSecret) {
        this.domain = domain;
        this.serviceId = serviceId;
        this.serviceSecret = serviceSecret;

        oauth = new OAuthToken();
        httpClient = HttpClient.newBuilder().build();
    }

    private URI uri(@NotNull String endpoint) {
        return URI.create("https://" + domain + endpoint);
    }

    private URI uri(@NotNull String endpoint, @NotNull Map<String, Object> payload) {
        return URI.create("https://" + domain + endpoint + SpaceQueryParameters.toQueryParameters(payload));
    }

    /**
     * Queries the given Space API endpoint using the specified method and payload, returns the response
     * as raw JSON.
     *
     * Depending on the method, the payload will be converted to either the URL query parameters or to the request body.
     *
     * @param endpoint the API endpoint, e.g. "/api/http/absences".
     * @param method the HTTP method, e.g. "GET".
     * @param payload the query parameters.
     * @return the response as raw JSON.
     * @throws IOException if the HTTP request throws it.
     * @throws InterruptedException if the HTTP request throws it.
     */
    JsonElement rawJSONQuery(@NotNull String endpoint, @NotNull String method,
                             @NotNull Map<String, Object> payload) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder().header("Accept", "application/json");
        if ("GET".equals(method)) {
            builder.method("GET", HttpRequest.BodyPublishers.noBody()).uri(uri(endpoint, payload));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(SpaceQueryParameters.toPostBody(payload)))
                    .uri(uri(endpoint)).setHeader("Content-Type", "application/json");
        }
        return rawJSONQuery(builder, Authorization.BEARER);
    }

    private JsonElement rawJSONQuery(@NotNull HttpRequest.Builder builder,
                                     @NotNull Authorization authorization) throws IOException, InterruptedException {
        applyAuthorization(builder, authorization);
        final HttpRequest request = builder.build();
        long start = System.currentTimeMillis();
        HttpResponse<String> response = null;
        for (int i = 0; i <= SERVER_ERROR_RETRIES; i++) {
            LOGGER.trace("Querying {}, attempt {}", request.uri(), i + 1);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                if (statusCode >= 500 && statusCode < 600) {
                    // server-side error, wait and retry
                    Thread.sleep(200);
                    LOGGER.trace("Response {}, retrying", statusCode);
                    continue;
                } else {
                    LOGGER.trace("Response {}, giving up", statusCode);
                    break;
                }
            }
            LOGGER.debug("Queried {} in {} ms", request.uri(), System.currentTimeMillis() - start);
            return JsonParser.parseString(response.body());
        }
        LOGGER.error("Failed to query {} in {} ms", request.uri(), System.currentTimeMillis() - start);
        throw new IOException("Failed to query " + request.uri() +", last response was " + response);
    }

    private enum Authorization {
        BASIC, BEARER
    }

    private void applyAuthorization(@NotNull HttpRequest.Builder builder, @NotNull Authorization authorization)
            throws IOException, InterruptedException {
        switch (authorization) {
            case BASIC:
                builder.setHeader("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString((serviceId + ":" + serviceSecret)
                                .getBytes(StandardCharsets.UTF_8)));
                break;
            case BEARER:
                oauth.refreshIfNeeded();
                builder.setHeader("Authorization", "Bearer " + oauth.token);
                break;
        }
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
     * - "query", a string query, String.
     * - "reportPastMembers", whether to include the members who are no longer active, boolean.
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
    public <T> ApiRequest<T> getObject(String endpoint, Class<T> objectType) {
        return new ObjectApiRequest<>(this, endpoint, "GET", objectType,
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
        return new ObjectApiRequest<>(this, endpoint, "GET",
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
        return new BatchApiRequest<>(this, endpoint, "GET", elementType);
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
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(uri("/oauth/token"))
                    .setHeader("Accept", "application/json")
                    .method("POST",
                            HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=**"))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded");
            JsonElement response = rawJSONQuery(requestBuilder, Authorization.BASIC);
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


}