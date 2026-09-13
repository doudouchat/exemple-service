package com.exemple.service.customer.core;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.customer.subscription.SubscriptionResource;

@Configuration
@ImportResource("classpath:exemple-service-customer.xml")
public class CustomerTestConfiguration {

    @Bean
    public AccountResource accountResource() {
        return mock(AccountResource.class);
    }

    @Bean
    public SubscriptionResource subscriptionResource() {
        return mock(SubscriptionResource.class);
    }

}
