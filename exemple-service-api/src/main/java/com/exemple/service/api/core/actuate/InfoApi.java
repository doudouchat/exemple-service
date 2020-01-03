package com.exemple.service.api.core.actuate;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.ManifestUtils;
import com.exemple.service.api.core.actuate.model.Info;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/")
@OpenAPIDefinition(tags = @Tag(name = "info"))
@Component
public class InfoApi {

    @Context
    private ServletContext servletContext;

    @GET
    @Template(name = "/info")
    @Produces({ MediaType.TEXT_XML, MediaType.TEXT_HTML })
    public Info template() throws IOException {

        return info();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "info")
    @ApiResponse(description = "Info Data", responseCode = "200", content = @Content(schema = @Schema(implementation = Info.class)))
    public Info info() throws IOException {

        Info result = new Info();
        result.setVersion(ManifestUtils.version(servletContext));
        result.setBuildTime(ManifestUtils.buildTime(servletContext));

        return result;
    }
}
