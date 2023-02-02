package com.exemple.service.api.core;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.api")
@ImportResource("classpath:exemple-service-api-security.xml")
public class ApiConfiguration {

    @Bean
    public MessageSource messageSource() {

        var messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setCacheSeconds(0);
        messageSource.setBasename("classpath:messages/erreur_messages");

        return messageSource;
    }

    @Bean
    public Validator validator() {

        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {

        var methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());
        methodValidationPostProcessor.setBeforeExistingAdvisors(true);

        return methodValidationPostProcessor;
    }

}
