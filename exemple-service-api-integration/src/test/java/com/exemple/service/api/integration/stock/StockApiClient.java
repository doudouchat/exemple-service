package com.exemple.service.api.integration.stock;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class StockApiClient {

    public static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private StockApiClient() {

    }

    public static Response post(String store, String product, Object body, String application) {

        return JsonRestTemplate.given().body(body)

                .header(APP_HEADER, application)

                .post(STOCK_URL, store, product);

    }

    public static Response get(String store, String product, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .get(STOCK_URL, store, product);

    }

}
