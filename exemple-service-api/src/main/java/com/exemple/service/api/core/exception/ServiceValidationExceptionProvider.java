package com.exemple.service.api.core.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.exemple.service.schema.common.exception.ValidationException;

@Provider
public class ServiceValidationExceptionProvider implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(e.getCauses()).build();
    }
}
