package com.exemple.service.customer.login.impl;

import org.springframework.stereotype.Service;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.customer.login.validation.LoginValidation;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

@Service
public class LoginServiceImpl implements LoginService {

    private final LoginResource loginResource;

    private final LoginValidation loginValidation;

    private final SchemaFilter schemaFilter;

    public LoginServiceImpl(LoginResource loginResource, LoginValidation loginValidation, SchemaFilter schemaFilter) {

        this.loginResource = loginResource;
        this.loginValidation = loginValidation;
        this.schemaFilter = schemaFilter;
    }

    @Override
    public boolean exist(String login) {
        return loginResource.get(login).isPresent();
    }

    @Override
    public void save(String login, ArrayNode patch) throws LoginServiceNotFoundException {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode old = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        JsonNode source = JsonPatch.apply(patch, old);

        loginValidation.validate(source, old, context.getApp(), context.getVersion(), context.getProfile());

        if (!login.equals(source.path(LoginField.USERNAME.field).textValue())) {

            try {
                loginResource.save(source);
            } catch (LoginResourceExistException e) {
                throw buildValidationException(e);
            }

            loginResource.delete(login);

        } else {

            loginResource.update(source);
        }

    }

    @Override
    public void save(JsonNode source) {

        ServiceContext context = ServiceContextExecution.context();

        loginValidation.validate(source, context.getApp(), context.getVersion(), context.getProfile());

        try {
            loginResource.save(source);
        } catch (LoginResourceExistException e) {
            throw buildValidationException(e);
        }

    }

    @Override
    public void delete(String login) {

        loginResource.delete(login);

    }

    @Override
    public JsonNode get(String login) throws LoginServiceNotFoundException {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode source = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        return schemaFilter.filter(context.getApp(), context.getVersion(), "login", context.getProfile(), source);
    }

    private static ValidationException buildValidationException(LoginResourceExistException exception) {

        ValidationException validationException = new ValidationException();

        ValidationExceptionModel cause = new ValidationExceptionModel(JsonPointer.compile(JsonPointer.SEPARATOR + LoginField.USERNAME.field), "login",
                "[".concat(exception.getLogin()).concat("] already exists"));

        validationException.add(cause);

        return validationException;
    }

}
