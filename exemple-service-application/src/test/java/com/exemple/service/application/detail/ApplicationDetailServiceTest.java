package com.exemple.service.application.detail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.core.ApplicationTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = { ApplicationTestConfiguration.class })
public class ApplicationDetailServiceTest extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ApplicationDetailService service;

    @Test
    public void put() {

        Map<String, Object> detail = new HashMap<>();
        detail.put("keyspace", "keyspace1");
        detail.put("company", "company1");
        detail.put("clientIds", Sets.newHashSet("clientId1"));
        detail.put("other", "other");

        service.put("app", MAPPER.convertValue(detail, JsonNode.class));

    }

    @Test(dependsOnMethods = "put")
    public void get() {

        ApplicationDetail detail = service.get("app");

        assertThat(detail.getKeyspace(), is("keyspace1"));
        assertThat(detail.getCompany(), is("company1"));
        assertThat(detail.getClientIds(), contains("clientId1"));

    }

    @Test
    public void getFailureNotFoundApplication() {

        String application = UUID.randomUUID().toString();

        try {

            service.get(application);

            Assert.fail("NotFoundApplicationException must be throwed");

        } catch (NotFoundApplicationException e) {

            assertThat(e.getApplication(), is(application));
        }

    }

}
