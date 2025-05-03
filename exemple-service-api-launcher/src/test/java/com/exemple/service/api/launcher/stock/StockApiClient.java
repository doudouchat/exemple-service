package com.exemple.service.api.launcher.stock;

import static com.exemple.service.api.launcher.core.InitData.APP_HEADER;

import com.exemple.service.api.launcher.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class StockApiClient {

    public static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private StockApiClient() {

    }

    public static Response increment(String store, String product, Long value, String application, String token) {

        return JsonRestTemplate.given(ContentType.TEXT).body(value)
                .header(APP_HEADER, application).header("Authorization", token)
                .post(STOCK_URL + "/_increment", store, product);

    }

    public static Response get(String store, String product, String application, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header("Authorization", token)
                .get(STOCK_URL, store, product);

    }

}
