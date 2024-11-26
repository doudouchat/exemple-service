package com.exemple.service.api.schema;

import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Path("/v1/schemas")
@Component
@RequiredArgsConstructor
public class SchemaApi {

    private final SchemaDescription service;

    @GET
    @Path("/{resource}/{app}/{version}/{profile}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode get(@NotNull @PathParam("resource") String resource, @NotNull @PathParam("app") String app,
            @NotNull @PathParam("version") String version, @NotNull @PathParam("profile") String profile) {

        ServiceContextExecution.setApp(app);

        return service.get(resource, version, profile);

    }

    @GET
    @Path("/patch")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode getPatch() {

        return service.getPatch();

    }
}
