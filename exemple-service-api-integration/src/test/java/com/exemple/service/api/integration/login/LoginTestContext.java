package com.exemple.service.api.integration.login;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class LoginTestContext {

    private final LinkedList<Response> responses;

    private final LinkedList<Response> postResponses;

    private final LinkedList<Response> getResponses;

    private final LinkedList<Response> deleteResponses;

    public LoginTestContext() {
        this.responses = new LinkedList<>();
        this.postResponses = new LinkedList<>();
        this.getResponses = new LinkedList<>();
        this.deleteResponses = new LinkedList<>();

    }

    public void savePost(Response post) {
        this.responses.add(post);
        this.postResponses.add(post);
    }

    public Response lastPost() {
        return this.postResponses.getLast();
    }

    public void saveGet(Response get) {
        this.responses.add(get);
        this.getResponses.add(get);
    }

    public Response lastGet() {
        return this.getResponses.getLast();
    }

    public Response lastDelete() {
        return this.deleteResponses.getLast();
    }

    public void saveDelete(Response delete) {
        this.responses.add(delete);
        this.deleteResponses.add(delete);
    }

    public Response lastResponse() {
        return this.responses.getLast();
    }

}
