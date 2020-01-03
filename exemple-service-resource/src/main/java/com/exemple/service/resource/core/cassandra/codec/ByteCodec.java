package com.exemple.service.resource.core.cassandra.codec;

import java.nio.ByteBuffer;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.data.ByteUtils;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.protocol.internal.util.Bytes;

public class ByteCodec implements TypeCodec<byte[]> {

    @Override
    public GenericType<byte[]> getJavaType() {
        return GenericType.of(byte[].class);
    }

    @Override
    public DataType getCqlType() {
        return DataTypes.BLOB;
    }

    @Override
    public ByteBuffer encode(byte[] value, ProtocolVersion protocolVersion) {
        return value != null ? ByteBuffer.wrap(value) : null;
    }

    @Override
    public byte[] decode(ByteBuffer value, ProtocolVersion protocolVersion) {
        return value != null ? Bytes.getArray(value) : null;
    }

    @Override
    public String format(byte[] value) {
        return ByteUtils.toHexString(value);
    }

    @Override
    public byte[] parse(String value) {
        return ByteUtils.fromHexString(value).array();
    }

}
