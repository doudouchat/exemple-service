package com.exemple.service.customer.login.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.core.script.CustomiseResourceHelper;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

@Service
public class LoginServiceImpl implements LoginService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String LOGIN = "login";

    private final LoginResource loginResource;

    private final SchemaValidation schemaValidation;

    private final SchemaFilter schemaFilter;

    private final CustomiseResourceHelper customiseResourceHelper;

    public LoginServiceImpl(LoginResource loginResource, SchemaValidation schemaValidation, SchemaFilter schemaFilter,
            CustomiseResourceHelper customiseResourceHelper) {

        this.loginResource = loginResource;
        this.schemaValidation = schemaValidation;
        this.schemaFilter = schemaFilter;
        this.customiseResourceHelper = customiseResourceHelper;
    }

    @Override
    public boolean exist(String login) {
        return loginResource.get(login).isPresent();
    }

    @Override
    public void save(String login, ArrayNode patch) throws LoginServiceNotFoundException {

        JsonNode old = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        JsonNode source = JsonPatch.apply(patch, old);

        validate(source, old);

        source = customiseResourceHelper.customise(LOGIN, source, old);

        if (usernameIsModified(login, source)) {

            createLogin(source);

            delete(login);

        } else {

            updateLogin(source);
        }

    }

    @Override
    public void save(JsonNode source) {

        validate(source);

        source = customiseResourceHelper.customise(LOGIN, source);

        createLogin(source);

    }

    @Override
    public void delete(String login) {

        loginResource.delete(login);

    }

    @Override
    public JsonNode get(String login) throws LoginServiceNotFoundException {

        JsonNode source = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        return filter(source);
    }

    @Override
    public ArrayNode get(UUID id) throws LoginServiceNotFoundException {

        List<JsonNode> sources = loginResource.get(id);

        if (sources.isEmpty()) {
            throw new LoginServiceNotFoundException();
        }

        ArrayNode logins = MAPPER.createArrayNode();

        filter(sources).forEach(logins::add);

        return logins;
    }

    private void createLogin(JsonNode source) {

        try {
            loginResource.save(source);
        } catch (LoginResourceExistException e) {
            throw buildValidationException(e);
        }
    }

    private void updateLogin(JsonNode source) {

        loginResource.update(source);
    }

    private JsonNode filter(JsonNode source) {

        ServiceContext context = ServiceContextExecution.context();

        return schemaFilter.filter(context.getApp(), context.getVersion(), LOGIN, context.getProfile(), source);
    }

    private List<JsonNode> filter(List<JsonNode> sources) {

        ServiceContext context = ServiceContextExecution.context();

        return sources.stream().map(source -> schemaFilter.filter(context.getApp(), context.getVersion(), LOGIN, context.getProfile(), source))
                .collect(Collectors.toList());
    }

    private void validate(JsonNode source) {

        ServiceContext context = ServiceContextExecution.context();

        schemaValidation.validate(context.getApp(), context.getVersion(), context.getProfile(), LOGIN, source);
    }

    private void validate(JsonNode source, JsonNode previousAccount) {

        ServiceContext context = ServiceContextExecution.context();

        schemaValidation.validate(context.getApp(), context.getVersion(), context.getProfile(), LOGIN, source, previousAccount);
    }

    private static boolean usernameIsModified(String login, JsonNode source) {

        return !login.equals(source.path(LoginField.USERNAME.field).textValue());
    }

    private static ValidationException buildValidationException(LoginResourceExistException exception) {

        ValidationException validationException = new ValidationException();

        ValidationExceptionModel cause = new ValidationExceptionModel(JsonPointer.compile(JsonPointer.SEPARATOR + LoginField.USERNAME.field), "login",
                "[".concat(exception.getLogin()).concat("] already exists"));

        validationException.add(cause);

        return validationException;
    }

}
