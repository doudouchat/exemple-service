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
import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
@Import(CustomerConfiguration.class)
public class CustomerTestConfiguration {

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

        SchemaFilter schemaFilter = Mockito.mock(SchemaFilter.class);

        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.init());

        return schemaFilter;
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
