package com.exemple.service.api.core.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class WebApplicationExceptionProvider implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {

        LOG.debug(ExceptionUtils.getStackTrace(e));
        return e.getResponse();

    }
}
