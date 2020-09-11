package com.exemple.service.customer.core;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.customer", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CustomerScriptConfiguration.class), @ComponentScan.Filter(Aspect.class) })
public class CustomerConfiguration {

}
