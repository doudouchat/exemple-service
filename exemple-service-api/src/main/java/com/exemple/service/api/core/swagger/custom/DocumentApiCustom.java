package com.exemple.service.api.core.swagger.custom;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class DocumentApiCustom extends AbstractSpecFilter {

    private static final String X_VERSION = "x_version";

    private static final String X_PROFILE = "x_profile";

    @Override
    public Optional<RequestBody> filterRequestBody(RequestBody requestBody, Operation operation, ApiDescription api, Map<String, List<String>> params,
            Map<String, String> cookies, Map<String, List<String>> headers) {

        buildVersioningSchema(requestBody.getContent(), headers);

        return Optional.of(requestBody);
    }

    @Override
    public Optional<ApiResponse> filterResponse(ApiResponse response, Operation operation, ApiDescription api, Map<String, List<String>> params,
            Map<String, String> cookies, Map<String, List<String>> headers) {

        buildVersioningSchema(response.getContent(), headers);

        return Optional.of(response);
    }

    private static void buildVersioningSchema(Map<String, MediaType> content, Map<String, List<String>> headers) {

        var mediaType = MapUtils.emptyIfNull(content).get(jakarta.ws.rs.core.MediaType.APPLICATION_JSON);

        if (mediaType != null && mediaType.getSchema() != null) {

            var name = StringUtils.substring(mediaType.getSchema().get$ref(), "#/components/schemas/".length());
            headers.entrySet().stream()
                    .filter(header -> StringUtils.equalsAnyIgnoreCase(header.getKey(), DocumentApiResource.RESOURCE + name))
                    .map(Map.Entry::getValue)
                    .filter(header -> !header.isEmpty())
                    .forEach((List<String> header) -> {

                        var composedSchema = new ComposedSchema();

                        header.stream().map((String version) -> {
                            String[] values = version.split("\\|");
                            return SchemaVersionProfileEntity.builder().version(values[0]).profile(values[1]).build();
                        }).forEach((SchemaVersionProfileEntity v) -> {

                            Schema<?> schema = new Schema<>();
                            schema.$ref("#/components/schemas/".concat(name + '.' + v.getVersion() + '.' + v.getProfile()));
                            composedSchema.addOneOfItem(schema);

                        });

                        mediaType.schema(composedSchema);
                    });
        }

    }

    @Override
    public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies,
            Map<String, List<String>> headers) {

        String host = headers.get(DocumentApiResource.APP_HOST).getFirst();

        Map<String, Object> extensions = MapUtils.emptyIfNull(schema.getExtensions());

        String version = (String) extensions.get(X_VERSION);
        if (version != null) {

            String profile = (String) extensions.get(X_PROFILE);
            var ref = new StringBuilder();
            ref.append(host)
                    .append("ws/v1/schemas/")
                    .append(schema.getName().toLowerCase(Locale.getDefault()))
                    .append('/')
                    .append(version)
                    .append('/')
                    .append(profile);
            schema.setName(StringUtils.capitalize(schema.getName()) + '.' + version + '.' + profile);
            schema.$ref(ref.toString());

        }

        if ("Patch".equals(schema.getName())) {

            schema.$ref(host.concat("ws/v1/schemas/patch"));

        }

        return Optional.of(schema);

    }

    @Override
    public Optional<OpenAPI> filterOpenAPI(OpenAPI openAPI, Map<String, List<String>> params, Map<String, String> cookies,
            Map<String, List<String>> headers) {

        headers.entrySet().stream()

                .filter((Map.Entry<String, List<String>> header) -> StringUtils.startsWith(header.getKey(), DocumentApiResource.RESOURCE))
                .forEach((Map.Entry<String, List<String>> header) -> {

                    var name = header.getKey().substring(DocumentApiResource.RESOURCE.length());

                    header.getValue().stream().map((String version) -> {
                        String[] values = version.split("\\|");
                        return SchemaVersionProfileEntity.builder().version(values[0]).profile(values[1]).build();
                    }).forEach((SchemaVersionProfileEntity v) -> {

                        Map<String, Object> extensions = Map.of(X_VERSION, v.getVersion(), X_PROFILE, v.getProfile());

                        Schema<?> schema = new Schema<>();
                        schema.setName(name);
                        schema.setExtensions(extensions);

                        openAPI.getComponents().addSchemas(StringUtils.capitalize(name) + '.' + v.getVersion() + '.' + v.getProfile(), schema);

                    });

                });

        return Optional.of(openAPI);
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return true;
    }
}
