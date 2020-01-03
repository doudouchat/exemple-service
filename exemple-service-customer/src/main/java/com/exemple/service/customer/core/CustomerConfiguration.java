package com.exemple.service.customer.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.customer")
@ImportResource("classpath:exemple-service-customer.xml")
public class CustomerConfiguration {

}
