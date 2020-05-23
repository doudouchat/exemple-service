package com.exemple.service.customer.core;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;

@Configuration
public class CustomerTestConfiguration extends CustomerConfiguration {

    static {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
        context.setProfile("default");
        context.setVersion("default");

    }

    @Bean
    public AccountResource accountResource() {
        return Mockito.mock(AccountResource.class);
    }

    @Bean
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public SchemaValidation schemaValidation() {
        return Mockito.mock(SchemaValidation.class);
    }

    @Bean
    public SchemaFilter schemaFilter() {
        return Mockito.mock(SchemaFilter.class);
    }

    @Bean
    public SchemaResource schemaResource() {
        return Mockito.mock(SchemaResource.class);
    }

    @Bean
    public SubscriptionResource subscriptionResource() {
        return Mockito.mock(SubscriptionResource.class);
    }

}
