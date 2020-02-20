package com.exemple.service.api.core.swagger.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.exemple.service.api.core.swagger.DocumentApiResource;

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

        MediaType mediaType = MapUtils.emptyIfNull(content).get(javax.ws.rs.core.MediaType.APPLICATION_JSON);

        if (mediaType != null) {

            String name = StringUtils.substring(mediaType.getSchema().get$ref(), "#/components/schemas/".length());
            headers.entrySet().stream()

                    .filter((Map.Entry<String, List<String>> header) -> StringUtils.equalsAnyIgnoreCase(header.getKey(),
                            DocumentApiResource.RESOURCE + name))
                    .forEach((Map.Entry<String, List<String>> header) -> {

                        ComposedSchema composedSchema = new ComposedSchema();

                        header.getValue().forEach((String version) -> {

                            Schema<?> schema = new Schema<>();
                            schema.$ref("#/components/schemas/".concat(name + '.' + version));
                            composedSchema.addOneOfItem(schema);

                        });

                        mediaType.schema(composedSchema);
                    });
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies,
            Map<String, List<String>> headers) {

        String host = headers.get(DocumentApiResource.APP_HOST).stream().findFirst().orElseThrow(IllegalArgumentException::new);
        String app = headers.get(DocumentApiResource.APP).stream().findFirst().orElseThrow(IllegalArgumentException::new);

        @SuppressWarnings("unchecked")
        Map<String, Object> extensions = MapUtils.emptyIfNull(schema.getExtensions());

        String version = (String) extensions.get(X_VERSION);
        if (version != null) {

            StringBuilder ref = new StringBuilder();
            ref.append(host);
            ref.append("ws/v1/schemas/");
            ref.append(schema.getName().toLowerCase(Locale.getDefault()));
            ref.append('/');
            ref.append(app);
            ref.append('/');
            ref.append(version);
            schema.setName(WordUtils.capitalize(schema.getName()) + '.' + version);
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

                    String name = header.getKey().substring(DocumentApiResource.RESOURCE.length());

                    header.getValue().forEach((String version) -> {

                        Map<String, Object> extensions = new HashMap<>();
                        extensions.put(X_VERSION, version);

                        Schema<?> schema = new Schema<>();
                        schema.setName(name);
                        schema.setExtensions(extensions);

                        openAPI.getComponents().addSchemas(WordUtils.capitalize(name) + '.' + version, schema);

                    });

                });

        return Optional.of(openAPI);
    }

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return true;
    }
}
