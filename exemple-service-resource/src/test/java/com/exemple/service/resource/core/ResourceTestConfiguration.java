package com.exemple.service.resource.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.cassandra.ResourceCassandraConfiguration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import(ResourceConfiguration.class)
@Slf4j
public class ResourceTestConfiguration extends ResourceCassandraConfiguration {

    @Value("${resource.cassandra.version}")
    private String version;

    private final Path cassandraResourcePath;

    public ResourceTestConfiguration(@Value("${resource.cassandra.resource_configuration}") String cassandraResource) throws FileNotFoundException {
        super(cassandraResource);
        this.cassandraResourcePath = Paths.get(ResourceUtils.getFile(cassandraResource).getPath());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public CassandraContainer embeddedServer() {

        return (CassandraContainer) new CassandraContainer("cassandra:" + version)
                .withExposedPorts(9042)
                .waitingFor(Wait.forLogMessage(".*Startup complete.*\\n", 1))
                .withLogConsumer(new Slf4jLogConsumer(LOG));
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
    @SneakyThrows
    public CqlSession session() {

        String content = new String(Files.readAllBytes(cassandraResourcePath), StandardCharsets.UTF_8);
        content = content.replaceAll("localhost:9042", "localhost:" + this.embeddedServer().getMappedPort(9042));
        Files.write(cassandraResourcePath, content.getBytes(StandardCharsets.UTF_8));

        return super.session();
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        Mockito.when(service.get(Mockito.anyString())).thenReturn(Optional.of(ApplicationDetail.builder().keyspace("test").build()));

        return service;
    }

    @PostConstruct
    public void initKeyspace() throws IOException {

        CqlSession session = this.session();

        session.setSchemaMetadataEnabled(false);

        executeScript("classpath:cassandra/keyspace.cql", session::execute);
        executeScript("classpath:cassandra/test.cql", session::execute);
        executeScript("classpath:cassandra/exec.cql", session::execute);

        session.setSchemaMetadataEnabled(true);

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

    private static void executeScript(String resourceLocation, Consumer<String> execute) throws IOException {
        Stream.of(FileUtils.readFileToString(ResourceUtils.getFile(resourceLocation), StandardCharsets.UTF_8).trim().split(";"))
                .forEach(execute);
    }

}
