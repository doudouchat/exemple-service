package com.exemple.service.api.core.exception;

import com.flipkart.zjsonpatch.JsonPatchApplicationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonPatchExceptionProvider implements ExceptionMapper<JsonPatchApplicationException> {

    @Override
    public Response toResponse(JsonPatchApplicationException e) {

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(e.getMessage()).build();
    }
}
