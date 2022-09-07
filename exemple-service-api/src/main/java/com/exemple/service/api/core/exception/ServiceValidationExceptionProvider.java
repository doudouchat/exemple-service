package com.exemple.service.api.core.exception;

import com.exemple.service.schema.common.exception.ValidationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ServiceValidationExceptionProvider implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(e.getCauses()).build();
    }
}
