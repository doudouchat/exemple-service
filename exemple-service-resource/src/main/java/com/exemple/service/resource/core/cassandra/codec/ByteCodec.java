package com.exemple.service.resource.core.cassandra.codec;

import java.nio.ByteBuffer;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.protocol.internal.util.Bytes;

public class ByteCodec extends MappingCodec<ByteBuffer, byte[]> implements TypeCodec<byte[]> {

    public ByteCodec() {
        super(TypeCodecs.BLOB, GenericType.of(byte[].class));
    }

    @Override
    protected byte[] innerToOuter(ByteBuffer value) {
        return value != null ? Bytes.getArray(value) : null;
    }

    @Override
    protected ByteBuffer outerToInner(byte[] value) {
        return value != null ? ByteBuffer.wrap(value) : null;
    }

}
