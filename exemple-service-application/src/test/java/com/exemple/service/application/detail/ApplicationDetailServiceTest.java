package com.exemple.service.application.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.core.ApplicationTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

@SpringJUnitConfig(ApplicationTestConfiguration.class)
public class ApplicationDetailServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ApplicationDetailService service;

    @Test
    @DisplayName("create application in zookeeper")
    public void createApplication() {

        // setup application
        Map<String, Object> application = new HashMap<>();
        application.put("keyspace", "keyspace1");
        application.put("company", "company1");
        application.put("clientIds", Sets.newHashSet("clientId1"));
        application.put("other", "other");

        // when save application
        service.put("app", MAPPER.convertValue(application, JsonNode.class));

        // Then retrieve application
        ApplicationDetail detail = service.get("app");

        // And check details
        assertAll(
                () -> assertThat(detail.getKeyspace()).isEqualTo("keyspace1"),
                () -> assertThat(detail.getCompany()).isEqualTo("company1"),
                () -> assertThat(detail.getClientIds()).containsOnly("clientId1"));
    }

    @Test
    @DisplayName("check exception if application is not found")
    public void getFailureNotFoundApplication() {

        // setup random application
        String application = UUID.randomUUID().toString();

        // When perform get
        Throwable throwable = catchThrowable(() -> service.get(application));

        // Then check throwable
        assertThat(throwable).isInstanceOf(NotFoundApplicationException.class);

    }

}
