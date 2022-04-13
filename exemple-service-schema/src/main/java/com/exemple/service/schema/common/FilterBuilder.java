package com.exemple.service.schema.common;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterBuilder {

    public static JsonNode filter(JsonNode origin, String... filter) {

        ObjectMapper mapper = new ObjectMapper();
        Squiggly.init(mapper, StringUtils.join(filter, ","));

        return mapper.convertValue(mapper.convertValue(origin, Map.class), JsonNode.class);

    }

}
