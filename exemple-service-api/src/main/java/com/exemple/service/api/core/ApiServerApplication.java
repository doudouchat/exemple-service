package com.exemple.service.api.core;

import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.customer.core.CustomerConfiguration;
import com.exemple.service.event.core.EventConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.exemple.service.schema.core.SchemaConfiguration;
import com.exemple.service.store.core.StoreConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
        CassandraAutoConfiguration.class })
@Import({ SchemaConfiguration.class, ApplicationConfiguration.class, CustomerConfiguration.class, StoreConfiguration.class,
        ResourceConfiguration.class })
public class ApiServerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiServerApplication.class);
    }

    @Configuration
    @Import(EventConfiguration.class)
    @ConditionalOnProperty(value = { "kafka.bootstrap-servers", "topic" }, prefix = "event")
    public static class EventApiConfiguration {

    }

    @Configuration
    @ConditionalOnClass(TestingServer.class)
    public static class ZookeeperConfiguration {

        private final int port;

        public ZookeeperConfiguration(@Value("${api.embedded.zookeeper.port:-1}") int port) {
            this.port = port;
        }

        @Bean(initMethod = "start", destroyMethod = "stop")
        public TestingServer embeddedZookeeper() throws Exception {

            return new TestingServer(port, false);
        }

    }

}
