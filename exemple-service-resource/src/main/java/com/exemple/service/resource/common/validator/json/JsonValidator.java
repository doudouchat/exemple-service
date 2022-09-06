package com.exemple.service.resource.common.validator.json;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.internal.core.type.PrimitiveType;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Maps;

import lombok.RequiredArgsConstructor;

@Component
public class JsonValidator {

    private static final String UNKNOWN_EXCEPTION = "UNKNOWN";

    private static final String ARRAY_EXCEPTION = "ARRAY";

    private final CqlSession session;

    private final List<AbstractJsonValidate<? extends DataType>> jsonValidates;

    public JsonValidator(CqlSession session) {
        this.session = session;

        this.jsonValidates = new LinkedList<>();
        this.jsonValidates.add(new JsonValidateMap());
        this.jsonValidates.add(new JsonValidateList());
        this.jsonValidates.add(new JsonValidateSet());
        this.jsonValidates.add(new JsonValidateUDT());
        this.jsonValidates.add(new JsonValidateTuple());
        this.jsonValidates.add(new JsonValidateStringOrInstant());
        this.jsonValidates.add(new JsonValidatePrimitiveNoStringAndNoInstant());

    }

    public void valid(JsonNode source, String table) throws JsonValidatorException {

        JsonStreamer<Map.Entry<String, JsonNode>> fields = source::fields;

        fields.forEach((Map.Entry<String, JsonNode> node) -> {

            var type = toDataType(node.getKey(), table);

            this.valid(type, node);
        });

    }

    private void valid(DataType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!json.getValue().isNull()) {

            List<AbstractJsonValidate<? extends DataType>> validates = jsonValidates.stream().filter(validate -> validate.hasChecked(dataType))
                    .toList();

            JsonStreamer<AbstractJsonValidate<DataType>> validateStreamer = () -> validates.stream()
                    .map(validate -> (AbstractJsonValidate<DataType>) validate)
                    .iterator();

            validateStreamer.forEach((AbstractJsonValidate<DataType> jsonValidate) -> jsonValidate.check(dataType, json));
        }

    }

    @RequiredArgsConstructor
    private abstract static class AbstractJsonValidate<T extends DataType> {

        private final Class<T> parameterType;

        protected boolean hasChecked(DataType dataType) {
            return parameterType.isAssignableFrom(dataType.getClass());
        }

        protected abstract void check(T dataType, Entry<String, JsonNode> json) throws JsonValidatorException;

    }

    private class JsonValidateMap extends AbstractJsonValidate<MapType> {

        public JsonValidateMap() {
            super(MapType.class);
        }

        @Override
        protected void check(MapType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            if (!json.getValue().isObject()) {
                throw new JsonValidatorException("OBJECT", json.getKey());
            }

            JsonStreamer<Map.Entry<String, JsonNode>> fields = json.getValue()::fields;

            fields.forEach((Entry<String, JsonNode> node) -> {
                var keyType = dataType.getKeyType();
                valid(keyType, Maps.immutableEntry(json.getKey(), new TextNode(node.getKey())));

                var valueType = dataType.getValueType();
                valid(valueType, node);

            });

        }

    }

    private class JsonValidateList extends AbstractJsonValidate<ListType> {

        public JsonValidateList() {
            super(ListType.class);
        }

        @Override
        protected void check(ListType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            if (!json.getValue().isArray()) {
                throw new JsonValidatorException(ARRAY_EXCEPTION, json.getKey());
            }

            JsonStreamer<JsonNode> elements = json.getValue()::elements;

            elements.forEach((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node)));

        }

    }

    private class JsonValidateSet extends AbstractJsonValidate<SetType> {

        public JsonValidateSet() {
            super(SetType.class);
        }

        @Override
        protected void check(SetType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            if (!json.getValue().isArray()) {
                throw new JsonValidatorException(ARRAY_EXCEPTION, json.getKey());
            }

            JsonStreamer<JsonNode> elements = json.getValue()::elements;

            elements.forEach((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node)));

        }

    }

    private class JsonValidateUDT extends AbstractJsonValidate<UserDefinedType> {

        public JsonValidateUDT() {
            super(UserDefinedType.class);
        }

        @Override
        protected void check(UserDefinedType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            JsonStreamer<Entry<String, JsonNode>> fields = json.getValue()::fields;

            fields.forEach((Entry<String, JsonNode> node) -> {

                if (!dataType.contains(node.getKey())) {
                    throw new JsonValidatorException(UNKNOWN_EXCEPTION, node.getKey());
                }

                DataType type = dataType.getFieldTypes().get(dataType.firstIndexOf(node.getKey()));
                valid(type, node);
            });

        }

    }

    private class JsonValidateTuple extends AbstractJsonValidate<TupleType> {

        public JsonValidateTuple() {
            super(TupleType.class);
        }

        @Override
        protected void check(TupleType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            Iterator<DataType> dataTypes = dataType.getComponentTypes().iterator();

            JsonStreamer<JsonNode> elements = json.getValue()::elements;

            elements.forEach((JsonNode node) -> {

                if (!dataTypes.hasNext()) {
                    throw new JsonValidatorException(UNKNOWN_EXCEPTION, json.getKey());
                }

                valid(dataTypes.next(), Maps.immutableEntry(json.getKey(), node));

            });

            if (dataTypes.hasNext()) {
                throw new JsonValidatorException("MISSING", json.getKey());
            }

        }

    }

    private class JsonValidateStringOrInstant extends AbstractJsonValidatePrimitive {

        @Override
        protected boolean hasChecked(PrimitiveType dataType) {
            return isStringOrInstant(dataType);
        }

        @Override
        protected void check(PrimitiveType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            if (!json.getValue().isTextual()) {
                throw new JsonValidatorException("VARCHAR", json.getKey());
            }

            var value = new StringBuilder().append("'").append(json.getValue().asText()).append("'").toString();
            checkPrimitive(value, dataType, json);

        }

    }

    private class JsonValidatePrimitiveNoStringAndNoInstant extends AbstractJsonValidatePrimitive {

        @Override
        protected boolean hasChecked(PrimitiveType dataType) {
            return !isStringOrInstant(dataType);
        }

        @Override
        protected void check(PrimitiveType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

            String value = json.getValue().asText();
            checkPrimitive(value, dataType, json);

        }

    }

    private abstract class AbstractJsonValidatePrimitive extends AbstractJsonValidate<PrimitiveType> {

        public AbstractJsonValidatePrimitive() {
            super(PrimitiveType.class);
        }

        @Override
        protected final boolean hasChecked(DataType dataType) {
            return super.hasChecked(dataType) && hasChecked((PrimitiveType) dataType);
        }

        abstract boolean hasChecked(PrimitiveType dataType);

    }

    private void checkPrimitive(String value, DataType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = toTypeCodec(dataType);
        try {
            typeCodec.parse(value);
        } catch (IllegalArgumentException e) {
            throw new JsonValidatorException(typeCodec.getCqlType().asCql(true, false), json.getKey(), e);
        }
    }

    private boolean isStringOrInstant(DataType dataType) {
        TypeCodec<Object> typeCodec = toTypeCodec(dataType);
        Class<Object> javaType = typeCodec.getJavaType().getRawType();
        return javaType.equals(String.class) || javaType.equals(java.time.Instant.class);
    }

    private TypeCodec<Object> toTypeCodec(DataType dataType) {

        return session.getContext().getCodecRegistry().codecFor(dataType);
    }

    private DataType toDataType(String key, String table) throws JsonValidatorException {

        var tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);
        return tableMetadata.getColumn(key).orElseThrow(() -> new JsonValidatorException(UNKNOWN_EXCEPTION, key)).getType();
    }

    @FunctionalInterface
    private interface JsonStreamer<T> extends Supplier<Iterator<T>> {

        default void forEach(JsonConsumer<T> action) throws JsonValidatorException {
            Iterator<T> nodes = get();
            while (nodes.hasNext()) {
                action.accept(nodes.next());
            }
        }

    }

    @FunctionalInterface
    private interface JsonConsumer<T> {

        void accept(T node) throws JsonValidatorException;

    }

}
