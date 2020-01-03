package com.exemple.service.resource.core.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableCaching
@ImportResource("classpath:exemple-service-resource.xml")
public class ResourceCacheConfiguration {

}
