package com.exemple.service.resource.core.cassandra.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.util.Strings;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JacksonJsonCodec<T> extends MappingCodec<ByteBuffer, T> implements TypeCodec<T> {

    private final ObjectMapper objectMapper;

    public JacksonJsonCodec(Class<T> javaClass) {
        this(javaClass, new ObjectMapper());
    }

    public JacksonJsonCodec(Class<T> javaClass, ObjectMapper jsonMapper) {
        super(TypeCodecs.BLOB, GenericType.of(javaClass));
        this.objectMapper = jsonMapper;
    }

    @Override
    public DataType getCqlType() {
        return DataTypes.TEXT;
    }

    @Override
    public String format(T value) {
        try {
            return Strings.quote(objectMapper.writeValueAsString(value));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    protected T innerToOuter(ByteBuffer value) {
        try {
            return value != null ? objectMapper.readValue(Bytes.getArray(value), toJacksonJavaType()) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    protected ByteBuffer outerToInner(T value) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private JavaType toJacksonJavaType() {
        return TypeFactory.defaultInstance().constructType(getJavaType().getType());
    }
}
