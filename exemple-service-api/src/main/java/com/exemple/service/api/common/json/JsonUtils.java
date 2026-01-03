package com.exemple.service.api.common.json;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode merge(JsonNode source, JsonNode override) {

        JsonNode defaults = MAPPER.readValue(source.toString(), JsonNode.class);
        ObjectReader updater = MAPPER.readerForUpdating(defaults);
        return updater.readValue(override);
    }

}
