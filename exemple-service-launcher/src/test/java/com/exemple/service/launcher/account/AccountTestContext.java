package com.exemple.service.launcher.account;

import java.util.LinkedList;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class AccountTestContext {

    private final LinkedList<UUID> ids;

    private final LinkedList<Response> responses;

    private final LinkedList<Response> postResponses;

    private final LinkedList<Response> getResponses;

    private final LinkedList<Response> patchResponses;

    private final LinkedList<Response> putResponses;

    public AccountTestContext() {
        this.ids = new LinkedList<>();
        this.responses = new LinkedList<>();
        this.postResponses = new LinkedList<>();
        this.getResponses = new LinkedList<>();
        this.patchResponses = new LinkedList<>();
        this.putResponses = new LinkedList<>();
    }

    public void saveId(UUID id) {
        this.ids.add(id);
    }

    public UUID lastId() {
        return this.ids.getLast();
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

    public void savePatch(Response patch) {
        this.responses.add(patch);
        this.patchResponses.add(patch);
    }

    public Response lastPatch() {
        return this.patchResponses.getLast();
    }

    public void savePut(Response put) {
        this.responses.add(put);
        this.putResponses.add(put);
    }

    public Response lastPut() {
        return this.patchResponses.getLast();
    }

    public Response lastResponse() {
        return this.responses.getLast();
    }

}
