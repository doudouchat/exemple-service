package com.exemple.service.application.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.core.ApplicationTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ApplicationTestConfiguration.class)
@ActiveProfiles("test")
class ApplicationDetailServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ApplicationDetailService service;

    @Test
    @DisplayName("create application in zookeeper")
    void createApplication() {

        // setup application
        var application = Map.of(
                "keyspace", "keyspace1",
                "company", "company1",
                "clientIds", Set.of("clientId1"),
                "other", "other");

        // when save application
        service.put("app", MAPPER.convertValue(application, JsonNode.class));

        // Then retrieve application
        var applicationDetail = service.get("app");

        // And check details
        var expectedApplicationDetail = ApplicationDetail.builder()
                .keyspace("keyspace1")
                .company("company1")
                .clientIds(Set.of("clientId1"))
                .build();
        assertThat(applicationDetail).get().usingRecursiveComparison()
                .isEqualTo(expectedApplicationDetail);
    }

    @Test
    @DisplayName("check exception if application is not found")
    void getFailureNotFoundApplication() {

        // setup random application
        var application = UUID.randomUUID().toString();

        // When perform get
        var applicationDetail = service.get(application);

        // Then check application is missing
        assertThat(applicationDetail).as("application % is unexpected", application).isEmpty();

    }

    @Test
    @DisplayName("check exception if application is incorrect")
    void getFailureBecauseApplicationIsIncorrect() {

        // setup save application
        service.put("fails", MAPPER.createArrayNode());

        // When perform get
        var throwable = catchThrowable(() -> service.get("fails"));

        // Then check throwable
        assertThat(throwable).isInstanceOf(IOException.class);
    }
}
