package com.exemple.service.api.integration.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

@Component
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

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test_keyspace");
        detail.setCompany("test_company");
        detail.setClientIds(Sets.newHashSet("test", "test_user"));

        Set<String> accountFilter = new HashSet<>();
        accountFilter.add("lastname");
        accountFilter.add("firstname");
        accountFilter.add("email");
        accountFilter.add("optin_mobile");
        accountFilter.add("civility");
        accountFilter.add("mobile");
        accountFilter.add("creation_date");
        accountFilter.add("update_date");
        accountFilter.add("birthday");
        accountFilter.add("addresses[*[city,street]]");
        accountFilter.add("cgus[code,version]");

        Set<String> accountField = new HashSet<>();
        accountField.add("lastname");
        accountField.add("firstname");
        accountField.add("email");
        accountField.add("optin_mobile");
        accountField.add("civility");
        accountField.add("mobile");
        accountField.add("creation_date");
        accountField.add("update_date");
        accountField.add("birthday");
        accountField.add("addresses[*[city,street]]");
        accountField.add("cgus[code,version]");

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setApplication(TEST_APP);
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("account.json").getInputStream())));
        accountSchema.setFilters(accountFilter);
        accountSchema.setFields(accountField);

        schemaResource.save(accountSchema);

        Set<String> subscriptionFilter = new HashSet<>();
        subscriptionFilter.add("subscription_date");

        Map<String, Set<String>> subscriptionRules = new HashMap<>();
        subscriptionRules.put("login", Collections.singleton("/email"));

        SchemaEntity subscriptionSchema = new SchemaEntity();
        subscriptionSchema.setApplication(TEST_APP);
        subscriptionSchema.setVersion(VERSION_V1);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setProfile("user");
        subscriptionSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("subscription.json").getInputStream())));
        subscriptionSchema.setFilters(subscriptionFilter);

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(TEST_APP, MAPPER.convertValue(detail, JsonNode.class));

        // STOCK

        ApplicationDetail backDetail = new ApplicationDetail();
        backDetail.setKeyspace("test_keyspace");
        backDetail.setCompany("test_company");
        backDetail.setClientIds(Sets.newHashSet("back", "back_user"));

        applicationDetailService.put(BACK_APP, MAPPER.convertValue(backDetail, JsonNode.class));

    }

    @PostConstruct
    public void initOther() throws IOException {

        // APP

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("other_keyspace");
        detail.setCompany("other_company");
        detail.setClientIds(Sets.newHashSet("test", "test_user"));

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        SchemaEntity accountSchema = new SchemaEntity();
        accountSchema.setApplication("other");
        accountSchema.setVersion(VERSION_V1);
        accountSchema.setResource("account");
        accountSchema.setProfile("user");
        accountSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("other.json").getInputStream())));

        schemaResource.save(accountSchema);

        applicationDetailService.put("other", MAPPER.convertValue(detail, JsonNode.class));

    }

}
