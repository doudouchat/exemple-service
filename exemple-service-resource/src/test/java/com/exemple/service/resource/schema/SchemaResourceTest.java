package com.exemple.service.resource.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ResourceTestConfiguration.class)
@ActiveProfiles("test")
class SchemaResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource resource;

    @BeforeEach
    void resetResourceContext() {

        ResourceExecutionContext.destroy();

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveSchema {

        private JsonNode schemaResource;

        @Test
        @Order(1)
        void save() throws IOException {

            // Given build schema

            schemaResource = MAPPER.readTree(new ClassPathResource("test.json").getContentAsByteArray());

            var resourceSchema = new SchemaEntity();
            resourceSchema.setVersion("v1");
            resourceSchema.setResource("account");
            resourceSchema.setProfile("example");
            resourceSchema.setContent(schemaResource);
            resourceSchema.setPatchs(Collections.singleton(MAPPER.convertValue(Collections.singletonMap("op", "add"), JsonNode.class)));

            // When perform save

            resource.save(resourceSchema);

            // Then check schema
            var expectedResourceSchema = new SchemaEntity();
            expectedResourceSchema.setVersion("v1");
            expectedResourceSchema.setResource("account");
            expectedResourceSchema.setProfile("example");
            expectedResourceSchema.setContent(schemaResource);
            expectedResourceSchema.setPatchs(Collections.singleton(MAPPER.convertValue(Collections.singletonMap("op", "add"), JsonNode.class)));
            assertThat(resource.get("account", "v1", "example")).get()
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResourceSchema);

        }

        @Test
        @Order(2)
        void cleanAll() {

            // Given build schema

            var resourceSchema = new SchemaEntity();
            resourceSchema.setVersion("v1");
            resourceSchema.setResource("account");
            resourceSchema.setProfile("example");

            // When perform update

            resource.update(resourceSchema);

            // Then check schema
            var expectedResourceSchema = new SchemaEntity();
            expectedResourceSchema.setVersion("v1");
            expectedResourceSchema.setResource("account");
            expectedResourceSchema.setProfile("example");
            expectedResourceSchema.setPatchs(Collections.emptySet());
            assertThat(resource.get("account", "v1", "example")).get()
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResourceSchema);
        }

    }

    @Test
    void allVersions() {

        // Given create schema

        var resourceSchema1 = new SchemaEntity();
        resourceSchema1.setVersion("v1");
        resourceSchema1.setResource("product");
        resourceSchema1.setProfile("example1");

        resource.save(resourceSchema1);

        // And create other schema

        var resourceSchema2 = new SchemaEntity();
        resourceSchema2.setVersion("v2");
        resourceSchema2.setResource("product");
        resourceSchema2.setProfile("example2");

        resource.save(resourceSchema2);

        // When perform all versions
        var versions = resource.allVersions("product");

        // Then check result
        var expectedSchemaVersionProfiles = List.of(
                SchemaVersionProfileEntity.builder().version("v1").profile("example1").build(),
                SchemaVersionProfileEntity.builder().version("v2").profile("example2").build());
        assertThat(versions)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedSchemaVersionProfiles);

    }

    @Test
    void getEmptySchema() {

        // When perform get
        var schemaResource = resource.get("account", UUID.randomUUID().toString(), "example");

        // Then check schema
        assertThat(schemaResource).isEmpty();

    }
}
