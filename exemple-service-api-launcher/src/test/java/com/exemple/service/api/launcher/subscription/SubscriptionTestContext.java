package com.exemple.service.api.launcher.subscription;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class SubscriptionTestContext {

    private final LinkedList<Response> responses;

    private final LinkedList<Response> putResponses;

    private final LinkedList<Response> getResponses;

    public SubscriptionTestContext() {
        this.responses = new LinkedList<>();
        this.putResponses = new LinkedList<>();
        this.getResponses = new LinkedList<>();
    }

    public void savePut(Response post) {
        this.responses.add(post);
        this.putResponses.add(post);
    }

    public Response lastPut() {
        return this.putResponses.getLast();
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
