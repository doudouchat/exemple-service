package com.exemple.service.resource.common.validator;

import java.util.Map;
import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.exemple.service.resource.common.JsonValidator;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.pivovarit.function.ThrowingConsumer;

public class JsonConstraintValidator implements ConstraintValidator<Json, JsonNode> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConstraintValidator.class);

    private static final String UNKNOWN = "UNKNOWN";

    private String table;

    private String messageTemplate;

    private final JsonValidator jsonValidator;

    private final CqlSession session;

    public JsonConstraintValidator(CqlSession session, JsonValidator jsonValidator) {
        this.session = session;
        this.jsonValidator = jsonValidator;
    }

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        if (source != null) {

            try {

                valid(source);

            } catch (JsonValidatorException e) {

                valid = false;

                LOG.trace(e.getMessage(messageTemplate), e);

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(e.getMessage(this.messageTemplate)).addPropertyNode(e.getNode())
                        .addConstraintViolation();
            }

        }

        return valid;
    }

    private void valid(JsonNode source) throws JsonValidatorException {

        JsonNodeUtils.stream(source.fields()).forEach(ThrowingConsumer.sneaky(this::valid));

    }

    private void valid(Map.Entry<String, JsonNode> node) throws JsonValidatorException {

        String key = node.getKey();

        TableMetadata tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);
        Optional<ColumnMetadata> column = tableMetadata.getColumn(key);

        if (!column.isPresent()) {
            throw new JsonValidatorException(UNKNOWN, key);
        }

        DataType type = column.get().getType();

        this.jsonValidator.valid(type, key, node.getValue());

    }

    @Override
    public void initialize(Json constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }

}
