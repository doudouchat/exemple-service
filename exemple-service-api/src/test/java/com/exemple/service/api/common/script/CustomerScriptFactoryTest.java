package com.exemple.service.api.common.script;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class CustomerScriptFactoryTest {

    private CustomerScriptFactory factory;

    @Mock
    private ApplicationDetailService applicationDetailService;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeMethod
    private void build() throws IOException {
        MockitoAnnotations.openMocks(this);
        factory = new CustomerScriptFactory(applicationDetailService, ResourceUtils.getFile("classpath:scripts").getAbsolutePath(),
                applicationContext);
        factory.initScriptApplicationContexts();

    }

    @Test
    public void getDefaultBean() throws AccountServiceNotFoundException {

        // Setup context
        ServiceContextExecution.context().setApp("test");

        // And init ApplicationDetail
        ApplicationDetail applicationDetail = new ApplicationDetail();
        applicationDetail.setCompany("default");
        Mockito.when(applicationDetailService.get("test")).thenReturn(applicationDetail);

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        JsonNode account = service.get(UUID.randomUUID());
        assertThat(account, hasJsonField("note", "default value"));

    }

    @Test
    public void getOverrideBean() throws AccountServiceNotFoundException {

        // Setup context
        ServiceContextExecution.context().setApp("test");

        // And init ApplicationDetail
        ApplicationDetail applicationDetail = new ApplicationDetail();
        applicationDetail.setCompany("company_test");
        Mockito.when(applicationDetailService.get("test")).thenReturn(applicationDetail);

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        JsonNode account = service.get(UUID.randomUUID());
        assertThat(account, hasJsonField("note", "override value"));

    }

}
