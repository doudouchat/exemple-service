package com.exemple.service.api.launcher.core;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.common.model.ApplicationDetail.AccountDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;

@Configuration
@DependsOn("initCassandra")
public class InitData {

    public static final String APP_HEADER = "app";

    public static final String BACK_APP = "back";

    public static final String TEST_APP = "test";

    public static final String ADMIN_APP = "admin";

    public static final String VERSION_HEADER = "version";

    public static final String VERSION_V1 = "v1";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @PostConstruct
    public void initSchema() throws IOException {

        // APP

        ApplicationDetail detail = ApplicationDetail.builder()
                .keyspace("test_keyspace")
                .company("test_company")
                .clientId("test")
                .clientId("test_user")
                .account(AccountDetail.builder().uniqueProperty("email")
                        .build())
                .build();

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(new ClassPathResource("account.json").getContentAsByteArray()));

        ObjectNode patchUpdateDate = MAPPER.createObjectNode();
        patchUpdateDate.put("op", "add");
        patchUpdateDate.put("path", "/properties/update_date");
        patchUpdateDate.set("value", MAPPER.readTree(
                """
                {"type": "string","format": "date-time","readOnly": true}"
                """));

        ObjectNode patchCreationDate = MAPPER.createObjectNode();
        patchCreationDate.put("op", "add");
        patchCreationDate.put("path", "/required/0");
        patchCreationDate.put("value", "creation_date");

        accountSchema.setPatchs(Set.of(patchUpdateDate, patchCreationDate));

        schemaResource.save(accountSchema);

        SchemaEntity subscriptionSchema = new SchemaEntity();
        subscriptionSchema.setVersion(VERSION_V1);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setProfile("user");
        subscriptionSchema.setContent(MAPPER.readTree(new ClassPathResource("subscription.json").getContentAsByteArray()));

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(TEST_APP, MAPPER.convertValue(detail, JsonNode.class));

        // STOCK

        ApplicationDetail backDetail = ApplicationDetail.builder()
                .keyspace("test_keyspace")
                .company("test_company")
                .clientId("back")
                .clientId("back_user")
                .build();

        applicationDetailService.put(BACK_APP, MAPPER.convertValue(backDetail, JsonNode.class));

    }

    @PostConstruct
    public void initOther() throws IOException {

        // APP

        ApplicationDetail detail = ApplicationDetail.builder()

                .keyspace("other_keyspace").company("other_company").clientId("test").clientId("test_user").build();

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(new ClassPathResource("other.json").getContentAsByteArray()));

        schemaResource.save(accountSchema);

        applicationDetailService.put("other", MAPPER.convertValue(detail, JsonNode.class));

    }

}
