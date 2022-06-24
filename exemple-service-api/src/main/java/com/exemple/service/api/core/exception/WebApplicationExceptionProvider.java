package com.exemple.service.api.core.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
