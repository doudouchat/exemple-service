package com.exemple.service.resource.stock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.store.stock.StockResource;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class StockResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private StockResource resource;

    @Test
    public void update() {

        resource.update("store1", "product1", 5L);
        resource.update("store1", "product1", -15L);

    }

    @Test(dependsOnMethods = "update")
    public void get() {

        assertThat(resource.get("store1", "product1"), is(-10L));

    }

    @Test
    public void getNotExist() {

        assertThat(resource.get(UUID.randomUUID().toString(), UUID.randomUUID().toString()), is(0L));

    }

}
