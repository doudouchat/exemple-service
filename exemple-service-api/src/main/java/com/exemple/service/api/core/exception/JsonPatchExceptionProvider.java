package com.exemple.service.api.core.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.flipkart.zjsonpatch.JsonPatchApplicationException;

@Provider
public class JsonPatchExceptionProvider implements ExceptionMapper<JsonPatchApplicationException> {

    @Override
    public Response toResponse(JsonPatchApplicationException e) {

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(e.getMessage()).build();
    }
}
