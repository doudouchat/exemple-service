package com.exemple.service.application.detail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.core.ApplicationTestConfiguration;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = { ApplicationTestConfiguration.class })
public class ApplicationDetailServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApplicationDetailService service;

    @Test
    public void put() {

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("keyspace1");
        detail.setCompany("company1");
        detail.setClientIds(Sets.newHashSet("clientId1"));

        service.put("app", detail);

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
