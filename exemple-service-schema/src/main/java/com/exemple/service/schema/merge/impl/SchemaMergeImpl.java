package com.exemple.service.schema.merge.impl;

import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.merge.SchemaMerge;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;
import com.google.common.collect.Streams;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaMergeImpl implements SchemaMerge {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    @Override
    public void mergeMissingFieldFromOriginal(String app, String version, String resource, String profile, JsonNode source, JsonNode orignalSource) {

        SchemaEntity schema = schemaResource.get(app, version, resource, profile);
        JsonNode originalSourceFilterBySchema = FilterBuilder.filter(orignalSource, schema.getFields().toArray(new String[0]));

        JsonNode patchs = JsonDiff.asJson(originalSourceFilterBySchema, orignalSource);

        Streams.stream(patchs.elements()).map((JsonNode patch) -> MAPPER.convertValue(new JsonNode[] { patch }, JsonNode.class))
                .forEach((JsonNode patch) -> SchemaMergeImpl.applyPatch(patch, source));

    }

    private static void applyPatch(JsonNode patch, JsonNode source) {

        try {
            JsonPatch.applyInPlace(patch, source);
        } catch (JsonPatchApplicationException e) {
            // NOP throw patch exception
        }
    }

}
