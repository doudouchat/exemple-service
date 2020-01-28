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

    @Value("${api.authorization.hazelcast.addresses}")
    private String[] addresses;

    @Value("${api.authorization.hazelcast.connectionTimeout:10000}")
    private int connectionTimeout;

    @Value("${api.authorization.hazelcast.connectionAttemptLimit:5}")
    private int connectionAttemptLimit;

    @Value("${api.authorization.hazelcast.connectionAttemptPeriod:5000}")
    private int connectionAttemptPeriod;

    @Bean
    public HazelcastInstance hazelcastInstance() {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(addresses);
        config.getNetworkConfig().setConnectionTimeout(connectionTimeout);
        config.getNetworkConfig().setConnectionAttemptLimit(connectionAttemptLimit);
        config.getNetworkConfig().setConnectionAttemptPeriod(connectionAttemptPeriod);

        return HazelcastClient.newHazelcastClient(config);
    }

}
