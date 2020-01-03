package com.exemple.service.api.core.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ExceptionProvider implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionProvider.class);

    @Override
    public Response toResponse(Exception e) {

        LOG.error(ExceptionUtils.getStackTrace(e));
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }
}
