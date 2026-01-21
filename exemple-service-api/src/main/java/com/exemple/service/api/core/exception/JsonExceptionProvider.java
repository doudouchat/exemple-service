package com.exemple.service.api.core.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import tools.jackson.core.JacksonException;

@Provider
public class JsonExceptionProvider implements ExceptionMapper<JacksonException> {

    @Override
    public Response toResponse(JacksonException e) {

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(e.getMessage()).build();
    }
}
