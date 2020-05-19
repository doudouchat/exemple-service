package com.exemple.service.api.core.swagger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.core.ApiContext;
import com.exemple.service.api.core.swagger.custom.DocumentApiCustom;
import com.exemple.service.api.core.swagger.security.DocumentApiSecurity;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;
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

    private DocumentApiSecurity documentApiSecurity;

    private final ApplicationDetailService applicationDetailService;

    private final SchemaResource schemaResource;

    private final ApiContext apiContext;

    public DocumentApiResource(ApplicationDetailService applicationDetailService, SchemaResource schemaResource, ApiContext apiContext) {

        OpenAPI openAPI = new OpenAPI();

        Info info = new Info();
        info.title("Api documentation");
        info.description("Api documentation");
        openAPI.setInfo(info);

        this.openApiConfiguration = new SwaggerConfiguration().filterClass(DocumentApiCustom.class.getName()).openAPI(openAPI);
        this.applicationDetailService = applicationDetailService;
        this.schemaResource = schemaResource;
        this.apiContext = apiContext;

    }

    @Autowired(required = false)
    public void setDocumentApiSecurity(DocumentApiSecurity documentApiSecurity) {
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

        Server server = new Server();
        server.setUrl(host);

        this.openApiConfiguration.getOpenAPI().addServersItem(server);

        ApplicationDetail applicationDetail = applicationDetailService.get(app);

        ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());

        schemaResource.allVersions(app).forEach(
                (String resource, List<SchemaVersionProfileEntity> versions) -> headers.getRequestHeaders().put(RESOURCE + resource, versions.stream()
                        .map((SchemaVersionProfileEntity v) -> v.getVersion().concat("|").concat(v.getProfile())).collect(Collectors.toList())));

        if (documentApiSecurity != null) {

            String ctxId = ServletConfigContextUtils.getContextIdFromServletConfig(config);
            @SuppressWarnings({ "rawtypes", "unchecked" })
            OpenApiContext ctx = new JaxrsOpenApiContextBuilder().servletConfig(config).application(application).resourcePackages(resourcePackages)
                    .configLocation(configLocation).openApiConfiguration(openApiConfiguration).ctxId(ctxId).buildContext(true);
            OpenAPI oas = ctx.read();

            oas.getComponents().setSecuritySchemes(documentApiSecurity.buildSecurityScheme());

        }

        return super.getOpenApi(headers, config, application, uriInfo, type);
    }

}
