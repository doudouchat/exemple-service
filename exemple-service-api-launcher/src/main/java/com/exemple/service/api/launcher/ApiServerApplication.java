package com.exemple.service.api.launcher;

import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.exemple.service.api.core.ApiConfiguration;
import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.exemple.service.schema.core.SchemaConfiguration;
import com.exemple.service.store.core.StoreConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
        CassandraAutoConfiguration.class })
@EnableCaching
@Import({ SchemaConfiguration.class, ApplicationConfiguration.class, StoreConfiguration.class, ResourceConfiguration.class, ApiConfiguration.class })
@PropertySource("classpath:default.properties")
public class ApiServerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiServerApplication.class);
    }
}
