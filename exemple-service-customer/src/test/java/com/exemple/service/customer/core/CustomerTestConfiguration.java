package com.exemple.service.customer.core;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.subscription.SubscriptionResource;

@Configuration
@ImportResource("classpath:exemple-service-customer.xml")
public class CustomerTestConfiguration {

    @Bean
    public AccountResource accountResource() {
        return Mockito.mock(AccountResource.class);
    }

    @Bean
    public SubscriptionResource subscriptionResource() {
        return Mockito.mock(SubscriptionResource.class);
    }

    @Bean
    public ResourceEventPublisher resourceEventPublisher() {
        return Mockito.mock(ResourceEventPublisher.class);
    }

}
