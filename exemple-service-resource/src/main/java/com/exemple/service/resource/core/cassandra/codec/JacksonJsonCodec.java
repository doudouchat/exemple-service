package com.exemple.service.resource.core.cassandra.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.util.Strings;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class JacksonJsonCodec<T> implements TypeCodec<T> {

    private final ObjectMapper objectMapper;
    private final GenericType<T> javaType;

    public JacksonJsonCodec(Class<T> javaClass) {
        this(javaClass, new ObjectMapper());
    }

    public JacksonJsonCodec(Class<T> javaClass, ObjectMapper jsonMapper) {
        this.javaType = GenericType.of(javaClass);
        this.objectMapper = jsonMapper;
    }

    @NonNull
    @Override
    public GenericType<T> getJavaType() {
        return javaType;
    }

    @NonNull
    @Override
    public DataType getCqlType() {
        return DataTypes.TEXT;
    }

    @Nullable
    @Override
    public ByteBuffer encode(@Nullable T value, @NonNull ProtocolVersion protocolVersion) {
        if (value == null) {
            return null;
        }
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    public T decode(@Nullable ByteBuffer bytes, @NonNull ProtocolVersion protocolVersion) {
        if (bytes == null) {
            return null;
        }
        try {
            return objectMapper.readValue(Bytes.getArray(bytes), toJacksonJavaType());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @NonNull
    @Override
    public String format(T value) {
        if (value == null) {
            return "NULL";
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return Strings.quote(json);
    }

    @Nullable
    @Override
    public T parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL")) {
            return null;
        }
        if (!Strings.isQuoted(value)) {
            throw new IllegalArgumentException("JSON strings must be enclosed by single quotes");
        }
        String json = Strings.unquote(value);
        try {
            return objectMapper.readValue(json, toJacksonJavaType());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private JavaType toJacksonJavaType() {
        return TypeFactory.defaultInstance().constructType(getJavaType().getType());
    }
}
