package com.exemple.service.api.core;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.description.SchemaDescription;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.merge.SchemaMerge;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
public class ApiTestConfiguration extends ApiConfiguration {

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
    public SchemaMerge schemaMerge() {
        return Mockito.mock(SchemaMerge.class);
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
    public JsonNode account() {

        return JsonNodeUtils.create("classpath:model/account.json");
    }

    @Bean(name = "login")
    public JsonNode login() {

        return JsonNodeUtils.create("classpath:model/login.json");
    }

    @Bean(name = "subscription")
    public JsonNode subscription() {

        return JsonNodeUtils.create("classpath:model/subscription.json");
    }

    @Bean(name = "schema")
    public JsonNode schema() {

        return JsonNodeUtils.create("classpath:model/schema.json");
    }

    @Bean(name = "swagger")
    public JsonNode swagger() {

        return JsonNodeUtils.create("classpath:model/swagger.json");
    }

    @Bean(name = "swagger_security")
    public JsonNode swaggerSecurity() {

        return JsonNodeUtils.create("classpath:model/swagger_security.json");
    }

}
