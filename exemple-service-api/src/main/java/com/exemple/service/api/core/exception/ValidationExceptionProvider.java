package com.exemple.service.api.core.exception;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@Component
@RequiredArgsConstructor
public class ValidationExceptionProvider implements ExceptionMapper<ConstraintViolationException> {

    private final MessageSource messageSource;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ConstraintViolationException e) {

        Map<String, String> messages = e.getConstraintViolations().stream().collect(
                Collectors.toMap((ConstraintViolation<?> violation) -> Iterables.getLast(violation.getPropertyPath()).getName(), this::getMessage));

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(messages).build();
    }

    private String getMessage(ConstraintViolation<?> violation) {

        return messageSource.getMessage(violation.getMessage(), new Object[0], violation.getMessage(),
                requestContext.getAcceptableLanguages().stream().findFirst().orElseGet(Locale::getDefault));
    }
}
