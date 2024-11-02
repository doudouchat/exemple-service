package com.exemple.service.store.core;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;

import com.exemple.service.store.stock.StockResource;

@Configuration
@Import(StoreConfiguration.class)
public class StoreTestConfiguration {

    @Bean
    public StockResource stockResource() {
        return Mockito.mock(StockResource.class);
    }

    @Bean
    public DynamicPropertyRegistrar applicationProperties(GenericContainer embeddedZookeeper) {
        return registry -> registry.add("store.zookeeper.host", () -> "127.0.0.1:" + embeddedZookeeper.getMappedPort(2181));
    }
}
