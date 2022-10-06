package com.exemple.service.api.common.script;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.service.application.detail.ApplicationDetailService;

@Configuration
@Import(CustomerScriptFactory.class)
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
