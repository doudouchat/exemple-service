package com.exemple.service.api.core.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebApplicationExceptionProvider implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionProvider.class);

    @Override
    public Response toResponse(WebApplicationException e) {

        LOG.debug(ExceptionUtils.getStackTrace(e));
        return e.getResponse();

    }
}
