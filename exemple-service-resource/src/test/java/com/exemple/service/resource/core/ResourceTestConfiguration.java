package com.exemple.service.resource.core;

import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.containers.GenericContainer;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.common.model.ApplicationDetail.AccountDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.cassandra.EmbeddedCassandraConfiguration;

import jakarta.validation.Validator;

@Configuration
@Import({ ResourceConfiguration.class, EmbeddedCassandraConfiguration.class })
public class ResourceTestConfiguration {

    @Autowired
    private CassandraContainer cassandraContainer;

    @Bean
    public DynamicPropertyRegistrar applicationProperties(@Qualifier("embeddedZookeeper") GenericContainer<?> embeddedZookeeper) {
        return registry -> registry.add("resource.zookeeper.host", () -> "127.0.0.1:" + embeddedZookeeper.getMappedPort(2181));
    }

    @Bean
    @Primary
    public CqlSession session(CqlSessionBuilder sessionBuilder) {

        return sessionBuilder
                .addContactPoint(cassandraContainer.getContactPoint())
                .withLocalDatacenter(cassandraContainer.getLocalDatacenter())
                .build();
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        Mockito.when(service.get(Mockito.anyString())).thenReturn(Optional.of(
                ApplicationDetail.builder()
                        .keyspace("test")
                        .account(AccountDetail.builder().uniqueProperty("email")
                                .build())
                        .build()));

        return service;
    }

    @Bean
    public Validator validator() {

        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());
        methodValidationPostProcessor.setBeforeExistingAdvisors(true);

        return methodValidationPostProcessor;
    }

}
