package com.exemple.service.resource.common.history;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SuperBuilder
@Getter
public abstract class ExpectedHistory<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private T id;

    private String field;

    private OffsetDateTime date;

    private JsonNode value;

    private JsonNode previousValue;

    public abstract HistoryModel<T> buildHistory(T id);

    public abstract static class ExpectedHistoryBuilder<T, C extends ExpectedHistory<T>, B extends ExpectedHistoryBuilder<T, C, B>> {

        public ExpectedHistoryBuilder() {
            this.value = MAPPER.nullNode();
            this.previousValue = MAPPER.nullNode();
        }

        @SneakyThrows
        public ExpectedHistoryBuilder<T, C, B> value(String value) {
            this.value = MAPPER.readTree(
                    """
                    "%s"
                    """.formatted(value));
            return this;
        }

        @SneakyThrows
        public ExpectedHistoryBuilder<T, C, B> value(Integer value) {
            this.value = MAPPER.readTree(
                    """
                    %s
                    """.formatted(value));
            return this;
        }

        public ExpectedHistoryBuilder<T, C, B> previousValue(JsonNode previousValue) {
            this.previousValue = previousValue;
            return this;
        }

        @SneakyThrows
        public ExpectedHistoryBuilder<T, C, B> previousValue(String previousValue) {
            this.previousValue = MAPPER.readTree(
                    """
                    "%s"
                    """.formatted(previousValue));
            return this;
        }

        @SneakyThrows
        public ExpectedHistoryBuilder<T, C, B> previousValue(Integer previousValue) {
            this.previousValue = MAPPER.readTree(
                    """
                    %s
                    """.formatted(previousValue));
            return this;
        }

    }
}