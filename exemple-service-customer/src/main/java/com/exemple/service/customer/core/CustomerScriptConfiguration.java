package com.exemple.service.customer.core;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.customer", includeFilters = @ComponentScan.Filter(Aspect.class))
public class CustomerScriptConfiguration {

}
