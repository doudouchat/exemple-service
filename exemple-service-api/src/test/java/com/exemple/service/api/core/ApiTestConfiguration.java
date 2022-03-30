package com.exemple.service.api.core;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.ResourceUtils;

import com.exemple.service.api.common.script.CustomerScriptFactory;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginResource;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.description.SchemaDescription;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ApiTestConfiguration extends ApiConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public AccountService accountService() {
        return Mockito.mock(AccountService.class);
    }

    @Bean
    public SchemaDescription schemaService() {
        return Mockito.mock(SchemaDescription.class);
    }

    @Bean
    public SchemaValidation schemaValidation() {
        return Mockito.mock(SchemaValidation.class);
    }

    @Bean
    public SchemaFilter schemaFilter() {

        return new SchemaFilter() {

            @Override
            public JsonNode filter(String app, String version, String resource, String profile, JsonNode source) {
                return source;
            }

        };

    }

    @Bean
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public StockService stockService() {
        return Mockito.mock(StockService.class);
    }

    @Bean
    public SchemaResource schemaResource() {
        return Mockito.mock(SchemaResource.class);
    }

    @Bean
    public SubscriptionService subscriptionService() {
        return Mockito.mock(SubscriptionService.class);
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {
        return Mockito.mock(ApplicationDetailService.class);
    }

    @Bean
    public CustomerScriptFactory customerScriptFactory(ApplicationContext context) {
        CustomerScriptFactory customerScriptFactory = Mockito.mock(CustomerScriptFactory.class);
        Mockito.when(customerScriptFactory.getBean(Mockito.anyString(), Mockito.eq(SubscriptionService.class)))
                .thenReturn(context.getBean(SubscriptionService.class));
        Mockito.when(customerScriptFactory.getBean(Mockito.anyString(), Mockito.eq(AccountService.class)))
                .thenReturn(context.getBean(AccountService.class));
        return customerScriptFactory;
    }

    @Bean
    public InitialContext initialContext() throws NamingException, IOException {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.SimpleContextFactory");
        System.setProperty(SimpleJndi.ENC, "java:comp");
        System.setProperty(JndiLoader.COLON_REPLACE, "--");
        System.setProperty(JndiLoader.DELIMITER, "/");
        System.setProperty(SimpleJndi.SHARED, "true");
        System.setProperty(SimpleJndi.ROOT, new ClassPathResource("java--comp").getURL().getFile());

        return new InitialContext();

    }

    @Bean
    @DependsOn("initialContext")
    @Override
    public JndiObjectFactoryBean jndiObjectFactoryBean() {

        return super.jndiObjectFactoryBean();
    }

    @Bean(name = "account")
    public JsonNode account() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/account.json"));
    }

    @Bean(name = "subscription")
    public JsonNode subscription() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/subscription.json"));

    }

    @Bean(name = "schema")
    public JsonNode schema() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/schema.json"));
    }

    @Bean(name = "swagger")
    public JsonNode swagger() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/swagger.json"));
    }

    @Bean(name = "swagger_security")
    public JsonNode swaggerSecurity() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/swagger_security.json"));
    }

}
