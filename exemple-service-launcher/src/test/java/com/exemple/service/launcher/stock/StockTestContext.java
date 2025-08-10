package com.exemple.service.launcher.stock;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;

@Component
@ScenarioScope
public class StockTestContext {

    private final LinkedList<Response> responses;

    private final LinkedList<Response> postResponses;

    private final LinkedList<Response> getResponses;

    public StockTestContext() {
        this.responses = new LinkedList<>();
        this.postResponses = new LinkedList<>();
        this.getResponses = new LinkedList<>();
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

    public Response lastResponse() {
        return this.responses.getLast();
    }

}
