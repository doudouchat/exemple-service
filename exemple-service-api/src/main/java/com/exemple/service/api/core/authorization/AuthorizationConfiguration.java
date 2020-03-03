package com.exemple.service.api.core.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@Profile("!noSecurity")
public class AuthorizationConfiguration {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final String[] addresses;

    private final int connectionTimeout;

    private final int initialBackoffMillis;

    private final int maxBackoffMillis;

    public AuthorizationConfiguration(@Value("${api.authorization.hazelcast.addresses}") String[] addresses,
            @Value("${api.authorization.hazelcast.connectionTimeout:10000}") int connectionTimeout,
            @Value("${api.authorization.hazelcast.initialBackoffMillis:2000}") int initialBackoffMillis,
            @Value("${api.authorization.hazelcast.maxBackoffMillis:6000}") int maxBackoffMillis) {

        this.addresses = addresses.clone();
        this.connectionTimeout = connectionTimeout;
        this.initialBackoffMillis = initialBackoffMillis;
        this.maxBackoffMillis = maxBackoffMillis;
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(addresses);
        config.getNetworkConfig().setConnectionTimeout(connectionTimeout);
        config.getConnectionStrategyConfig().getConnectionRetryConfig().setInitialBackoffMillis(initialBackoffMillis);
        config.getConnectionStrategyConfig().getConnectionRetryConfig().setMaxBackoffMillis(maxBackoffMillis);

        return HazelcastClient.newHazelcastClient(config);
    }

}
