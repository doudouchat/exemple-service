package com.exemple.service.api.core;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.exemple.service.api.core.authorization.AuthorizationService;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.description.SchemaDescription;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

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

        SchemaFilter schemaFilter = Mockito.mock(SchemaFilter.class);

        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.init());

        return schemaFilter;
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
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean(name = "authorizationServiceImpl")
    @ConditionalOnNotWebApplication
    public AuthorizationService AuthorizationService() {
        return Mockito.mock(AuthorizationService.class);
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        detail.setCompany("company1");
        detail.setClientIds(Sets.newHashSet("clientId1"));

        Mockito.when(service.get(Mockito.anyString())).thenReturn(detail);

        return service;
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

}
