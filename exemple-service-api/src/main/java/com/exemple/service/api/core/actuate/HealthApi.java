package com.exemple.service.api.core.actuate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import com.exemple.service.api.core.actuate.model.Health;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/health")
@OpenAPIDefinition(tags = @Tag(name = "health"))
@Component
public class HealthApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "health")
    @ApiResponse(description = "UP!", responseCode = "200", content = @Content(schema = @Schema(implementation = Health.class)))
    public Response health() {

        Health result = new Health();
        result.setStatus("UP");

        return Response.status(Status.OK).entity(result).build();
    }
}
