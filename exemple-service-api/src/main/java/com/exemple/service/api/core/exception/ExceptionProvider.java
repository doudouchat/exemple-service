package com.exemple.service.api.core.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class ExceptionProvider implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {

        LOG.error(ExceptionUtils.getStackTrace(e));
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }
}
