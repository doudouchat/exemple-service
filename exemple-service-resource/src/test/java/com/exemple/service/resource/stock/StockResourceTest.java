package com.exemple.service.resource.stock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.store.stock.StockResource;

@SpringJUnitConfig(ResourceTestConfiguration.class)
public class StockResourceTest {

    @Autowired
    private StockResource resource;

    @Test
    public void update() {

        // when update stock
        resource.update("store1", "product1", 5L);

        // And update again stock
        resource.update("store1", "product1", -15L);

        // Then check stock
        assertThat(resource.get("store1", "product1").get(), is(-10L));

    }

    @Test
    public void getNotExist() {

        assertThat(resource.get(UUID.randomUUID().toString(), UUID.randomUUID().toString()).isPresent(), is(false));

    }

}
