package com.exemple.service.api.core;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.util.ResourceUtils;

import com.exemple.service.api.common.script.CustomerScriptFactory;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.description.SchemaDescription;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@Import(ApiConfiguration.class)
public class ApiTestConfiguration {

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
    public SchemaBuilder schemaBuilder() {
        return Mockito.mock(SchemaBuilder.class);
    }

    @Bean
    public SchemaFilter schemaFilter() {

        return new SchemaFilter(null) {

            @Override
            public JsonNode filter(String resource, String version, String profile, JsonNode source) {
                return source;
            }

            @Override
            public JsonNode filterAllProperties(String resource, String version, String profile, JsonNode source) {
                return source;
            }

            @Override
            public JsonNode filterAllAdditionalProperties(String resource, String version, String profile, JsonNode source) {
                return MAPPER.nullNode();
            }

        };

    }

    @Bean
    public LoginService loginService() {
        return Mockito.mock(LoginService.class);
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
        Mockito.when(customerScriptFactory.getBean(Mockito.anyString(), Mockito.eq(SubscriptionService.class), Mockito.anyString()))
                .thenReturn(context.getBean(SubscriptionService.class));
        Mockito.when(customerScriptFactory.getBean(Mockito.anyString(), Mockito.eq(AccountService.class), Mockito.anyString()))
                .thenReturn(context.getBean(AccountService.class));
        Mockito.when(customerScriptFactory.getBean(Mockito.anyString(), Mockito.eq(LoginService.class), Mockito.anyString()))
                .thenReturn(context.getBean(LoginService.class));
        return customerScriptFactory;
    }

    @Bean(name = "account")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JsonNode account() throws IOException {

        return MAPPER.readTree(ResourceUtils.getFile("classpath:model/account.json"));
    }

    @Bean(name = "subscription")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
