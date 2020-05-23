package com.exemple.service.api.schema;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;

@Path("/v1/schemas")
@Component
public class SchemaApi {

    private final SchemaDescription service;

    public SchemaApi(SchemaDescription service) {

        this.service = service;
    }

    @GET
    @Path("/{resource}/{app}/{version}/{profile}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode get(@NotNull @PathParam("resource") String resource, @NotNull @PathParam("app") String app,
            @NotNull @PathParam("version") String version, @NotNull @PathParam("profile") String profile) {

        return service.get(app, version, resource, profile);

    }

    @GET
    @Path("/patch")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(hidden = true)
    public JsonNode getPatch() {

        return service.getPatch();

    }
}
