package com.exemple.service.customer.core;

import org.mockito.Mockito;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.subscription.SubscriptionResource;

@Configuration
@Import(CustomerConfiguration.class)
public class CustomerTestConfiguration {

    @Bean
    public AccountResource accountResource() {
        return Mockito.mock(AccountResource.class);
    }

    @Bean
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public SchemaResource schemaResource() {
        return Mockito.mock(SchemaResource.class);
    }

    @Bean
    public SubscriptionResource subscriptionResource() {
        return Mockito.mock(SubscriptionResource.class);
    }

    @Bean
    public ApplicationDetailService applicationDetailService() {
        ApplicationDetailService applicationDetailService = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail defaultApplication = new ApplicationDetail();
        defaultApplication.setCompany("default");
        Mockito.when(applicationDetailService.get("default")).thenReturn(defaultApplication);

        ApplicationDetail testApplication = new ApplicationDetail();
        testApplication.setCompany("test");
        Mockito.when(applicationDetailService.get("test")).thenReturn(testApplication);

        return applicationDetailService;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-customer-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

}
