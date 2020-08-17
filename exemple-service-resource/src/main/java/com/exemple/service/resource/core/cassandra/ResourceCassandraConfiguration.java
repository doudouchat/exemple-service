package com.exemple.service.resource.core.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.codec.ExtraTypeCodecs;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
public class ResourceCassandraConfiguration {

    private final String[] addresses;

    private final int port;

    private final String localDataCenter;

    public ResourceCassandraConfiguration(@Value("${resource.cassandra.addresses}") String[] addresses, @Value("${resource.cassandra.port}") int port,
            @Value("${resource.cassandra.local_data_center}") String localDataCenter) {

        this.addresses = addresses.clone();
        this.port = port;
        this.localDataCenter = localDataCenter;
    }

    @Bean
    public CqlSession session() {

        return CqlSession.builder().withLocalDatacenter(localDataCenter)
                .addContactPoints(Arrays.stream(addresses).map((String address) -> new InetSocketAddress(address, port)).collect(Collectors.toList()))
                .build();
    }

    @PostConstruct
    public void init() {

        MutableCodecRegistry registry = (MutableCodecRegistry) session().getContext().getCodecRegistry();
        registry.register(ExtraTypeCodecs.json(JsonNode.class));
        registry.register(ExtraTypeCodecs.BLOB_TO_ARRAY);

    }

}
