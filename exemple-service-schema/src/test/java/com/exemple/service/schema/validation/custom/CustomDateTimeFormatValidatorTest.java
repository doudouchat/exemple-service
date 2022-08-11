package com.exemple.service.schema.validation.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CustomDateTimeFormatValidatorTest {

    private static Schema schema;

    @BeforeAll
    public static void buildSchema() {

        JSONObject schemaJson = new JSONObject(new JSONTokener("{\n"
                + "        \"$schema\": \"http://json-schema.org/draft-07/schema\",\n"
                + "        \"properties\": {\n"
                + "                \"creation_date\": {\n"
                + "                        \"type\": \"string\",\n"
                + "                        \"format\": \"date-time\"\n"
                + "                },\n"
                + "        },\n"
                + "}"));

        schema = SchemaLoader.builder().draftV7Support().schemaJson(schemaJson)
                .addFormatValidator(new CustomDateTimeFormatValidator()).enableOverrideOfBuiltInFormatValidators().build().load().build();

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2018-02-28T12:00:00Z",
            "2018-02-28 12:00:00Z" })
    void dateTimeSuccess(String value) {

        // When perform
        Throwable throwable = catchThrowable(() -> schema.validate(new JSONObject("{\"creation_date\" : \"" + value + "\"}")));

        // Then check none exception
        assertThat(throwable).as("None exception is expected").isNull();

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "aazaza",
            "2018-02-29T12:00:00Z",
            "2018-02-30T12:00:00Z",
            "2018-02-30 12:00:00Z",
            "2018-02-2812:00:00Z" })
    void dateTimeFailure(String value) {

        // When perform
        Throwable throwable = catchThrowable(() -> schema.validate(new JSONObject("{\"creation_date\" : \"" + value + "\"}")));

        // Then check throwable
        assertThat(throwable).isInstanceOf(ValidationException.class);
    }

}
