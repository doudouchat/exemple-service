package com.exemple.service.store.core;

import org.apache.curator.test.TestingServer;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.resource.stock.StockResource;

@Configuration
public class StoreTestConfiguration extends StoreConfiguration {

    @Value("${store.zookeeper.port}")
    private int port;

    @Bean(destroyMethod = "stop")
    public TestingServer embeddedZookeeper() throws Exception {

        return new TestingServer(port, true);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-store-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public StockResource stockResource() {
        return Mockito.mock(StockResource.class);
    }
}
