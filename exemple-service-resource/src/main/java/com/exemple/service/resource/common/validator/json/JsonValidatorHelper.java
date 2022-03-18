package com.exemple.service.resource.common.validator.json;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.exemple.service.resource.common.JsonValidatorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonValidatorHelper {

    private final CqlSession session;

    public void valid(DataType dataType, String value, String root) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = toTypeCodec(dataType);

        Class<Object> javaType = typeCodec.getJavaType().getRawType();

        checkTypeCodecAndValue(typeCodec, javaType, value, root);
    }

    public void valid(DataType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = toTypeCodec(dataType);

        Class<Object> javaType = typeCodec.getJavaType().getRawType();

        checkIfCondition(!isStringOrInstant(javaType) || JsonNodeType.STRING == json.getValue().getNodeType(), "VARCHAR", json.getKey());
        checkTypeCodecAndValue(typeCodec, javaType, json.getValue().asText(), json.getKey());
    }

    private static void checkTypeCodecAndValue(TypeCodec<Object> typeCodec, Class<Object> javaType, Object value, String root)
            throws JsonValidatorException {

        String valueString = String.valueOf(value);
        if (isStringOrInstant(javaType)) {
            valueString = new StringBuilder().append("'").append(valueString).append("'").toString();
        }
        try {
            typeCodec.parse(valueString);
        } catch (IllegalArgumentException e) {
            throw new JsonValidatorException(typeCodec.getCqlType().asCql(true, false), root, e);
        }
    }

    private static boolean isStringOrInstant(Class<Object> javaType) {
        return javaType.equals(String.class) || javaType.equals(java.time.Instant.class);
    }

    private TypeCodec<Object> toTypeCodec(DataType dataType) {

        return session.getContext().getCodecRegistry().codecFor(dataType);
    }

    protected static void checkIfCondition(boolean condition, String keyIfException, String nodeIfException) throws JsonValidatorException {
        if (!condition) {
            throw new JsonValidatorException(keyIfException, nodeIfException);
        }
    }

}
