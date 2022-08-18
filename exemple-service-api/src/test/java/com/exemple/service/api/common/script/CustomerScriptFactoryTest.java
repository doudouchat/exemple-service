package com.exemple.service.api.common.script;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CustomerScriptFactoryTest {

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
    void getDefaultBean() throws IOException {

        // Given init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company("default").build()));

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class, "test");

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        Optional<JsonNode> account = service.get(UUID.randomUUID());
        assertThat(account).hasValue(mapper.readTree("{\"note\": \"default value\"}"));

    }

    @Test
    void getOverrideBean() throws IOException {

        // Given init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company("company_test").build()));

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class, "test");

        ObjectMapper mapper = new ObjectMapper();
        mapper.createObjectNode().put("note", "default value");

        // then check get response
        Optional<JsonNode> account = service.get(UUID.randomUUID());
        assertThat(account).hasValue(mapper.readTree("{\"note\": \"override value\"}"));

    }

}
