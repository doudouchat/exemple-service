package com.exemple.service.resource.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.exemple.service.resource.core.cassandra.ResourceCassandraConfiguration;

@Configuration
@EnableAspectJAutoProxy
@Import(ResourceCassandraConfiguration.class)
@ComponentScan(basePackages = "com.exemple.service.resource")
public class ResourceConfiguration {

}
