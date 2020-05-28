package com.exemple.service.resource.core;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.mockito.Mockito;
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
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.cassandra.ResourceCassandraConfiguration;
import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.artifact.Artifact;

@Configuration
@Import(ResourceConfiguration.class)
public class ResourceTestConfiguration extends ResourceCassandraConfiguration {

    @Value("${resource.cassandra.port}")
    private int port;

    @Value("${resource.cassandra.version}")
    private String version;

    public ResourceTestConfiguration(@Value("${resource.cassandra.addresses}") String[] addresses, @Value("${resource.cassandra.port}") int port,
            @Value("${resource.cassandra.local_data_center}") String localDataCenter) {

        super(addresses, port, localDataCenter);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Cassandra embeddedServer() {

        EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
        cassandraFactory.setArtifact(Artifact.ofVersion(version));
        cassandraFactory.setPort(port);
        cassandraFactory.getEnvironmentVariables().put("MAX_HEAP_SIZE", "64M");
        cassandraFactory.getEnvironmentVariables().put("HEAP_NEWSIZE", "12m");
        cassandraFactory.getConfigProperties().put("num_tokens", 1);
        cassandraFactory.getConfigProperties().put("initial_token", 0);
        cassandraFactory.getConfigProperties().put("hinted_handoff_enabled", false);

        return cassandraFactory.create();
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

        CqlSession session = super.session();
        session.setSchemaMetadataEnabled(true);
        return session;
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        Mockito.when(service.get(Mockito.anyString())).thenReturn(detail);

        return service;
    }

    @PostConstruct
    public void initKeyspace() {

        CqlSessionCassandraConnectionFactory cassandraConnectionFactory = new CqlSessionCassandraConnectionFactory();

        try (CassandraConnection connection = cassandraConnectionFactory.create(embeddedServer())) {
            CqlDataSet.ofClasspaths("cassandra/keyspace.cql", "cassandra/test.cql", "cassandra/exec.cql").forEachStatement(connection::execute);
        }

        ServiceContextExecution.context().setApp("test");
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
