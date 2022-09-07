package com.exemple.service.api.core.exception;

import com.exemple.service.application.common.exception.NotFoundApplicationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundApplicationExceptionProvider implements ExceptionMapper<NotFoundApplicationException> {

    @Override
    public Response toResponse(NotFoundApplicationException e) {

        return Response.status(Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).entity("Access to " + e.getApplication() + " is forbidden")
                .build();
    }
}
