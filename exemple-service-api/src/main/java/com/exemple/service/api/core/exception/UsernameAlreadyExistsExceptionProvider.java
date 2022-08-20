package com.exemple.service.api.core.exception;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.UsernameAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Provider
@Component
@RequiredArgsConstructor
public class UsernameAlreadyExistsExceptionProvider implements ExceptionMapper<UsernameAlreadyExistsException> {

    @Override
    public Response toResponse(UsernameAlreadyExistsException exception) {

        Map<String, Object> cause = Map.of("code", "username", "message", exception.getMessage());

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(Collections.singletonList(cause)).build();

    }

}
