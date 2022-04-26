package com.exemple.service.api.launcher.actuate;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class ActuatorTestContext {

    private final LinkedList<Response> responses;

    private final LinkedList<Response> getResponses;

    public ActuatorTestContext() {
        this.responses = new LinkedList<>();
        this.getResponses = new LinkedList<>();
    }

    public void saveGet(Response get) {
        this.responses.add(get);
        this.getResponses.add(get);
    }

    public Response lastGet() {
        return this.getResponses.getLast();
    }

    public Response lastResponse() {
        return this.responses.getLast();
    }

}
