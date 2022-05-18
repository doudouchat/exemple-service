package com.exemple.service.api.common.script;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class CustomerScriptFactoryTest {

    private CustomerScriptFactory factory;

    @Mock
    private ApplicationDetailService applicationDetailService;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    private void build() throws IOException {
        MockitoAnnotations.openMocks(this);
        factory = new CustomerScriptFactory(applicationDetailService, ResourceUtils.getFile("classpath:scripts").getAbsolutePath(),
                applicationContext);
        factory.initScriptApplicationContexts();

    }

    @Test
    public void getDefaultBean() throws IOException {

        // Setup context
        ServiceContextExecution.context().setApp("test");

        // And init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company("default").build()));

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        Optional<JsonNode> account = service.get(UUID.randomUUID());
        assertThat(account).hasValue(mapper.readTree("{\"note\": \"default value\"}"));

    }

    @Test
    public void getOverrideBean() throws IOException {

        // Setup context
        ServiceContextExecution.context().setApp("test");

        // And init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company("company_test").build()));

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        Optional<JsonNode> account = service.get(UUID.randomUUID());
        assertThat(account).hasValue(mapper.readTree("{\"note\": \"override value\"}"));

    }

}
