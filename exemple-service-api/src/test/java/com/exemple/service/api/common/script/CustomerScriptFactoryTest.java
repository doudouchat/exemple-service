package com.exemple.service.api.common.script;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.customer.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

@SpringBootTest(classes = CustomerScriptTestConfiguration.class)
@TestPropertySource(properties = { "customer.contexts.path=${java.io.tmpdir}/scripts", "customer.contexts.delay=2000" })
@ActiveProfiles({ "test", "CustomerScriptFactoryTest" })
class CustomerScriptFactoryTest {

    private static final String SCRIPTS_DIRECTORY_PATH = SystemUtils.JAVA_IO_TMPDIR + "/scripts";

    @Autowired
    private CustomerScriptFactory factory;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @BeforeAll
    static void createScriptDirectory() throws IOException {
        ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH).delete();
        ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH).mkdir();
    }

    @BeforeAll
    static void deleteScriptDirectory() throws IOException {
        ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH).delete();
    }

    @Test
    @DisplayName("Application has not specific script")
    void noScript() throws IOException {

        // Given init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company("default").build()));

        // when perform
        AccountService service = factory.getBean("accountService", AccountService.class, "test");

        ObjectMapper mapper = new ObjectMapper();

        // then check get response
        Optional<JsonNode> account = service.get(UUID.randomUUID());
        assertThat(account).hasValue(mapper.readTree(
                """
                {"note": "default value"}
                """));
    }

    @Test
    @DisplayName("Application has specific script")
    void withScript() throws IOException {

        // Given application test
        String application = "application1";

        // Setup copy scripts
        ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH + "/" + application).mkdir();
        File companyTestScript = ResourceUtils.getFile("classpath:scripts/company_test/exemple-service-customer.xml");
        Files.copy(companyTestScript, ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH + "/" + application + "/" + companyTestScript.getName()));

        // And init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company(application).build()));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            // when perform
            AccountService service = factory.getBean("accountService", AccountService.class, "test");

            ObjectMapper mapper = new ObjectMapper();

            // then check get response
            Optional<JsonNode> account = service.get(UUID.randomUUID());
            assertThat(account).hasValue(mapper.readTree(
                    """
                    {"note": "override value"}
                    """));

        });

    }

    @Test
    @DisplayName("Application has specific script and modify in runtime this script")
    void modifyScript() throws IOException {

        // Given application test
        String application = "application2";

        // Setup copy scripts
        ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH + "/" + application).mkdir();
        File script = ResourceUtils.getFile(SCRIPTS_DIRECTORY_PATH + "/" + application + "/exemple-service-customer.xml");
        Files.copy(ResourceUtils.getFile("classpath:scripts/company_test/exemple-service-customer.xml"), script);

        // And init ApplicationDetail
        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().company(application).build()));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            // when perform
            AccountService service = factory.getBean("accountService", AccountService.class, "test");

            ObjectMapper mapper = new ObjectMapper();

            // then check get response
            Optional<JsonNode> account = service.get(UUID.randomUUID());
            assertThat(account).hasValue(mapper.readTree(
                    """
                    {"note": "override value"}
                    """));

        });

        // And modify script
        String scriptContent = FileUtils.readFileToString(script, Charset.defaultCharset());
        scriptContent = scriptContent.replace("override value", "new override value");
        FileUtils.writeStringToFile(script, scriptContent, Charset.defaultCharset());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            // when perform
            AccountService service = factory.getBean("accountService", AccountService.class, "test");

            ObjectMapper mapper = new ObjectMapper();

            // then check get response
            Optional<JsonNode> account = service.get(UUID.randomUUID());
            assertThat(account).hasValue(mapper.readTree(
                    """
                    {"note": "new override value"}
                    """));

        });

    }

}
