package com.exemple.service.api.common.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode merge(JsonNode source, JsonNode override) throws IOException {

        JsonNode defaults = MAPPER.readValue(source.toString(), JsonNode.class);
        ObjectReader updater = MAPPER.readerForUpdating(defaults);
        return updater.readValue(override);
    }

}
