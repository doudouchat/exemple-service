package com.exemple.service.resource.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonNodeFilterUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void clean() throws IOException {

        // Given build account
        JsonNode account = MAPPER.readTree("{"
                + "  \"birthday\" : null,"
                + "  \"addresses\" : {"
                + "    \"job\" : null,"
                + "    \"home\" : {"
                + "      \"street\" : \"1 rue de de la poste\","
                + "      \"city\" : null,"
                + "      \"zip\" : null,"
                + "      \"floor\" : null,"
                + "      \"enable\" : null"
                + "    }"
                + "  },"
                + "  \"preferences\" : [ [ \"pref1\", 10 ], null ],"
                + "  \"address\" : {"
                + "    \"street\" : \"1 rue de la paix\","
                + "    \"city\" : null,"
                + "    \"zip\" : null,"
                + "    \"floor\" : null,"
                + "    \"enable\" : null"
                + "  },"
                + "  \"profils\" : [ null, \"profil 1\" ],"
                + "  \"cgus\" : [ null, {"
                + "    \"code\" : \"code_1\","
                + "    \"version\" : null"
                + "  } ],"
                + "  \"email\" : \"jean.dupont@gmail.com\""
                + "}");

        // When perform clean
        JsonNode source = JsonNodeFilterUtils.clean(account);

        // Then check clean source
        JsonNode expectedResult = MAPPER.readTree("{"
                + "  \"addresses\" : {"
                + "    \"home\" : {"
                + "      \"street\" : \"1 rue de de la poste\""
                + "    }"
                + "  },"
                + "  \"preferences\" : [ [ \"pref1\", 10 ] ],"
                + "  \"address\" : {"
                + "    \"street\" : \"1 rue de la paix\""
                + "  },"
                + "  \"profils\" : [ \"profil 1\" ],"
                + "  \"cgus\" : [ {"
                + "    \"code\" : \"code_1\""
                + "  } ],"
                + "  \"email\" : \"jean.dupont@gmail.com\""
                + "}");

        assertThat(expectedResult).isEqualTo(source);

    }

}
