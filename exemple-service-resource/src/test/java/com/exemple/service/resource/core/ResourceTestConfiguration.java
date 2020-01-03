package com.exemple.service.resource.core;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.cassandra.ResourceCassandraConfiguration;

import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.embedded.CassandraShutDownHook;

@Configuration
@Import(ResourceConfiguration.class)
public class ResourceTestConfiguration extends ResourceCassandraConfiguration {

    private CassandraShutDownHook cassandraShutDownHook = new CassandraShutDownHook();

    @Value("${resource.cassandra.port}")
    private int port;

    @Bean(initMethod = "buildServer")
    public CassandraEmbeddedServerBuilder embeddedServer() {

        System.setProperty("cassandra.unsafesystem", "true");
        System.setProperty("com.datastax.driver.FORCE_NIO", "true");

        return CassandraEmbeddedServerBuilder.builder().withScript("cassandra/keyspace.cql").withScript("cassandra/test.cql")
                .withScript("cassandra/exec.cql").withClusterName("test").withCQLPort(port).withShutdownHook(cassandraShutDownHook)
                .cleanDataFilesAtStartup(true);

    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-resource-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    @DependsOn("embeddedServer")
    public CqlSession session() {

        return super.session();
    }

    @PostConstruct
    public void initKeyspace() {

        ResourceExecutionContext.get().setKeyspace("test");

    }

    @Bean
    public Validator validator() {

        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());

        return methodValidationPostProcessor;
    }

}
