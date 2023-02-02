package com.exemple.service.api.core.exception;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.resource.account.exception.UsernameAlreadyExistsException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@Component
@RequiredArgsConstructor
public class UsernameAlreadyExistsExceptionProvider implements ExceptionMapper<UsernameAlreadyExistsException> {

    private static final String EXCEPTION_MESSAGE = "[{0}] already exists";

    @Override
    public Response toResponse(UsernameAlreadyExistsException exception) {

        Map<String, Object> cause = Map.of(
                "code", "username",
                "message", MessageFormat.format(EXCEPTION_MESSAGE, exception.getUsername()));

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(Collections.singletonList(cause)).build();

    }

}
