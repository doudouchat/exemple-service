package com.exemple.service.customer.login.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.exemple.service.customer.core.script.CustomiseResourceHelper;
import com.exemple.service.customer.core.script.CustomiseValidationHelper;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String LOGIN = "login";

    private final LoginResource loginResource;

    private final CustomiseResourceHelper customiseResourceHelper;

    private final CustomiseValidationHelper customiseValidationHelper;

    public LoginServiceImpl(LoginResource loginResource, CustomiseResourceHelper customiseResourceHelper,
            CustomiseValidationHelper customiseValidationHelper) {

        this.loginResource = loginResource;
        this.customiseResourceHelper = customiseResourceHelper;
        this.customiseValidationHelper = customiseValidationHelper;
    }

    @Override
    public boolean exist(String username) {
        return loginResource.get(username).isPresent();
    }

    @Override
    public void save(String username, JsonNode source, JsonNode previousSource) throws LoginServiceAlreadyExistException {

        customiseValidationHelper.validate(LOGIN, source, previousSource);

        JsonNode login = customiseResourceHelper.customise(LOGIN, source, previousSource);

        if (usernameIsModified(username, login)) {

            createLogin(login);

            delete(username);

        } else {

            updateLogin(login);
        }

    }

    @Override
    public void save(JsonNode source) throws LoginServiceAlreadyExistException {

        customiseValidationHelper.validate(LOGIN, source);

        JsonNode login = customiseResourceHelper.customise(LOGIN, source);

        createLogin(login);

    }

    @Override
    public void delete(String username) {

        loginResource.delete(username);

    }

    @Override
    public JsonNode get(String login) throws LoginServiceNotFoundException {

        return loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);
    }

    @Override
    public List<JsonNode> get(UUID id) throws LoginServiceNotFoundException {

        List<JsonNode> sources = loginResource.get(id);

        if (sources.isEmpty()) {
            throw new LoginServiceNotFoundException();
        }

        return sources;
    }

    private void createLogin(JsonNode source) throws LoginServiceAlreadyExistException {

        try {
            loginResource.save(source);
        } catch (LoginResourceExistException e) {
            throw new LoginServiceAlreadyExistException(e.getLogin(), e);
        }
    }

    private void updateLogin(JsonNode source) {

        loginResource.update(source);
    }

    private static boolean usernameIsModified(String login, JsonNode source) {

        return !login.equals(source.path(LoginField.USERNAME.field).textValue());
    }

}
