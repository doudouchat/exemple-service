package com.exemple.service.api.core.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.exemple.service.application.common.exception.NotFoundApplicationException;

@Provider
public class NotFoundApplicationExceptionProvider implements ExceptionMapper<NotFoundApplicationException> {

    @Override
    public Response toResponse(NotFoundApplicationException e) {

        return Response.status(Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).entity("Access to " + e.getApplication() + " is forbidden")
                .build();
    }
}
