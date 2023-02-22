package com.exemple.service.store.core;

import org.apache.curator.test.TestingServer;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.service.store.stock.StockResource;

@Configuration
@Import(StoreConfiguration.class)
public class StoreTestConfiguration {

    @Bean(destroyMethod = "stop")
    public TestingServer embeddedZookeeper(@Value("${store.zookeeper.port}") int port) throws Exception {

        return new TestingServer(port, true);
    }

    @Bean
    public StockResource stockResource() {
        return Mockito.mock(StockResource.class);
    }
}
