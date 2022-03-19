package com.exemple.service.event.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.event.publisher")
@RequiredArgsConstructor
public class EventPublisherConfiguration {

}
