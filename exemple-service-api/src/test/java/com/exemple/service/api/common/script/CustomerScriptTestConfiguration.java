package com.exemple.service.api.common.script;

import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.core.CustomerConfigurationProperties;

@Configuration
@EnableConfigurationProperties(CustomerConfigurationProperties.class)
@Import(CustomerScriptFactory.class)
@Profile("CustomerScriptFactoryTest")
public class CustomerScriptTestConfiguration {

    @Bean
    public ApplicationDetailService applicationDetailService() {
        return Mockito.mock(ApplicationDetailService.class);
    }

    @Bean
    public ApplicationContext applicationContext() {
        return Mockito.mock(ApplicationContext.class);
    }

}
