package com.exemple.service.api.core.swagger;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.exemple.service.api.core.ApiContext;
import com.exemple.service.api.core.swagger.custom.DocumentApiCustom;
import com.exemple.service.api.core.swagger.security.DocumentApiSecurity;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.ServletConfigContextUtils;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/{app}/openapi.{type:json|yaml}")
@Component
public class DocumentApiResource extends BaseOpenApiResource {

    public static final String APP_HOST = "app_host";

    public static final String APP = "app";

    public static final String RESOURCE = "resource:";

    public static final String BEARER_AUTH = "bearer_authentification";

    public static final String OAUTH2_CLIENT_CREDENTIALS = "oauth2_client_credentials";

    public static final String OAUTH2_PASS = "oauth2_password";

    @Context
    private ServletConfig config;

    @Context
    private Application application;

    private final DocumentApiSecurity documentApiSecurity;

    private final SchemaResource schemaResource;

    private final ApiContext apiContext;

    public DocumentApiResource(SchemaResource schemaResource, ApiContext apiContext, DocumentApiSecurity documentApiSecurity) {

        var openAPI = new OpenAPI();

        var info = new Info();
        info.title("Api documentation");
        info.description("Api documentation");
        openAPI.setInfo(info);

        this.openApiConfiguration = new SwaggerConfiguration().filterClass(DocumentApiCustom.class.getName())
                .resourcePackages(Collections.singleton("com.exemple.service.api")).openAPI(openAPI);
        this.schemaResource = schemaResource;
        this.apiContext = apiContext;
        this.documentApiSecurity = documentApiSecurity;

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, "application/yaml" })
    @Operation(hidden = true)
    public Response getOpenApi(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("type") String type, @PathParam("app") String app)
            throws Exception {

        String host = uriInfo.getBaseUri().getPath().replace("/ws", "");

        headers.getRequestHeaders().put(APP, Collections.singletonList(app));
        headers.getRequestHeaders().put(APP_HOST, Collections.singletonList(host));

        this.openApiConfiguration.getOpenAPI().getInfo().setVersion(apiContext.getVersion());

        var server = new Server();
        server.setUrl(host);

        this.openApiConfiguration.getOpenAPI().addServersItem(server);

        schemaResource.allVersions(app).forEach(
                (String resource, List<SchemaVersionProfileEntity> versions) -> headers.getRequestHeaders().put(
                        RESOURCE + resource,
                        versions.stream().map((SchemaVersionProfileEntity v) -> v.getVersion().concat("|").concat(v.getProfile())).toList()));

        String ctxId = ServletConfigContextUtils.getContextIdFromServletConfig(config);
        OpenApiContext ctx = new JaxrsOpenApiContextBuilder<>().servletConfig(config).application(application).resourcePackages(resourcePackages)
                .configLocation(configLocation).openApiConfiguration(openApiConfiguration).ctxId(ctxId).buildContext(true);
        OpenAPI oas = ctx.read();

        oas.getComponents().setSecuritySchemes(documentApiSecurity.buildSecurityScheme());

        return super.getOpenApi(headers, config, application, uriInfo, type);
    }

}
