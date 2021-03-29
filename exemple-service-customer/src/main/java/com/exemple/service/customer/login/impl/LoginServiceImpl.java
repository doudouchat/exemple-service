package com.exemple.service.customer.login.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.exemple.service.customer.core.script.CustomiseResourceHelper;
import com.exemple.service.customer.core.script.CustomiseValidationHelper;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import com.pivovarit.function.ThrowingConsumer;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String LOGIN = "login";

    private final LoginResource loginResource;

    private final CustomiseResourceHelper customiseResourceHelper;

    private final CustomiseValidationHelper customiseValidationHelper;

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    public void save(JsonNode source, JsonNode previousSource) {

        Assert.isTrue(source.path(LoginField.USERNAME.field).equals(previousSource.path(LoginField.USERNAME.field)), "Username must be equals");

        customiseValidationHelper.validate(LOGIN, source, previousSource);

        JsonNode login = customiseResourceHelper.customise(LOGIN, source, previousSource);

        updateLogin(login);

    }

    @Override
    public void save(ArrayNode sources, ArrayNode previousSources) throws LoginServiceAlreadyExistException {

        Map<String, JsonNode> sourcesMap = Streams.stream(sources.elements())
                .collect(Collectors.toMap((JsonNode source) -> source.get(LoginField.USERNAME.field).textValue(), Function.identity()));

        Map<String, JsonNode> previousSource = Streams.stream(previousSources.elements())
                .collect(Collectors.toMap((JsonNode source) -> source.get(LoginField.USERNAME.field).textValue(), Function.identity()));

        // check login
        Optional<String> usernameAlreadyExist = sourcesMap.entrySet().stream()
                .filter((Map.Entry<String, JsonNode> node) -> !previousSource.containsKey(node.getKey())).map(Map.Entry::getKey).filter(this::exist)
                .findFirst();
        if (usernameAlreadyExist.isPresent()) {
            throw new LoginServiceAlreadyExistException(usernameAlreadyExist.get());
        }

        // add login
        sourcesMap.entrySet().stream().filter((Map.Entry<String, JsonNode> node) -> !previousSource.containsKey(node.getKey()))
                .map(Map.Entry::getValue).forEach(ThrowingConsumer.sneaky(this::save));

        // update login
        sourcesMap.entrySet().stream().filter((Map.Entry<String, JsonNode> node) -> previousSource.containsKey(node.getKey()))
                .forEach((Map.Entry<String, JsonNode> node) -> this.save(node.getValue(), previousSource.get(node.getKey())));

        // delete login
        previousSource.entrySet().stream().filter((Map.Entry<String, JsonNode> node) -> !sourcesMap.containsKey(node.getKey())).map(Map.Entry::getKey)
                .forEach(this::delete);

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
    public ArrayNode get(UUID id) throws LoginServiceNotFoundException {

        List<JsonNode> sources = loginResource.get(id);
        sources.sort((JsonNode login1, JsonNode login2) -> login1.get(LoginField.USERNAME.field).textValue()
                .compareTo(login2.get(LoginField.USERNAME.field).textValue()));

        if (sources.isEmpty()) {
            throw new LoginServiceNotFoundException();
        }

        return MAPPER.createArrayNode().addAll(sources);
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
}
