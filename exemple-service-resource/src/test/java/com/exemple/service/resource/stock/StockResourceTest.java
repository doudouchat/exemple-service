package com.exemple.service.resource.stock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.stock.history.StockHistoryResource;
import com.exemple.service.resource.stock.model.StockHistory;
import com.exemple.service.store.stock.StockResource;

@SpringBootTest(classes = ResourceTestConfiguration.class)
@ActiveProfiles("test")
class StockResourceTest {

    @Autowired
    private StockResource resource;

    @Autowired
    private StockHistoryResource historyResource;

    @BeforeEach
    void initExecutionContextDate() {

        ServiceContextExecution.setPrincipal(() -> "user");
        ServiceContextExecution.setApp("test");

    }

    @Test
    void update() {

        // when update stock
        resource.update("store1", "product1", 5L);

        // And update again stock
        resource.update("store1", "product1", -15L);

        // Then check stock
        assertThat(resource.get("store1", "product1")).hasValue(-10L);

        // And check history
        var expectedHistory1 = new StockHistory();
        expectedHistory1.setApplication("test");
        expectedHistory1.setUser("user");
        expectedHistory1.setStore("store1");
        expectedHistory1.setProduct("product1");
        expectedHistory1.setQuantity(5L);

        var expectedHistory2 = new StockHistory();
        expectedHistory2.setApplication("test");
        expectedHistory2.setUser("user");
        expectedHistory2.setStore("store1");
        expectedHistory2.setProduct("product1");
        expectedHistory2.setQuantity(-15L);

        assertThat(historyResource.findByStoreAndProduct("store1", "product1")).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("date")
                .isEqualTo(List.of(expectedHistory1, expectedHistory2));

    }

    @Test
    void getNotExist() {

        assertThat(resource.get(UUID.randomUUID().toString(), UUID.randomUUID().toString())).isEmpty();

    }

}
