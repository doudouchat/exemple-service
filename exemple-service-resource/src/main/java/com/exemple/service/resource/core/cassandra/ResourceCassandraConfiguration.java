package com.exemple.service.resource.core.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.exemple.service.resource.core.cassandra.codec.ByteCodec;
import com.exemple.service.resource.core.cassandra.codec.JacksonJsonCodec;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
public class ResourceCassandraConfiguration {

    @Value("${resource.cassandra.addresses}")
    private String[] addresses;

    @Value("${resource.cassandra.port}")
    private int port;

    @Value("${resource.cassandra.local_data_center}")
    private String localDataCenter;

    @Bean
    public CqlSession session() {

        return CqlSession.builder().withLocalDatacenter(localDataCenter)
                .addContactPoints(Arrays.stream(addresses).map((String address) -> new InetSocketAddress(address, port)).collect(Collectors.toList()))
                .build();
    }

    @PostConstruct
    public void init() {

        TypeCodec<JsonNode> jsonNodeCodec = new JacksonJsonCodec<>(JsonNode.class);

        MutableCodecRegistry registry = (MutableCodecRegistry) session().getContext().getCodecRegistry();
        registry.register(jsonNodeCodec);
        registry.register(new ByteCodec());

    }

}
