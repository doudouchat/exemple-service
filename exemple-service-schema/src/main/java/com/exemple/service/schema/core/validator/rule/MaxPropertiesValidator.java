package com.exemple.service.schema.core.validator.rule;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class MaxPropertiesValidator implements ValidatorService {

    @Override
    public void validate(String value, JsonNode form, JsonNode old, ValidationException validationException) {

        String[] values = StringUtils.split(value, ",");
        String path = values[0];
        int maxProperties = Integer.parseInt(values[1]);

        JsonNode pathNode = form.at(path);

        if (JsonNodeType.OBJECT == pathNode.getNodeType() && !validationException.contains(path)) {

            Set<String> fields = filter(pathNode.fields()).collect(Collectors.toSet());

            if (old != null) {

                Map<String, JsonNode> mergeNode = JsonNodeUtils.stream(old.at(path).fields())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                JsonNodeUtils.stream(pathNode.fields()).forEach(n -> mergeNode.put(n.getKey(), n.getValue()));

                filter(mergeNode.entrySet().iterator()).forEach(fields::add);
            }

            if (fields.size() > maxProperties) {

                ValidationExceptionModel exception = new ValidationExceptionModel(path, "maxProperties", "maximum size: ["
                        .concat(Integer.toString(maxProperties)).concat("], found: [".concat(Integer.toString(fields.size())).concat("]")));

                validationException.add(exception);

            }
        }

    }

    private static Stream<String> filter(Iterator<Entry<String, JsonNode>> fields) {
        return JsonNodeUtils.stream(fields).filter(e -> JsonNodeType.NULL != e.getValue().getNodeType()).map(Map.Entry::getKey);
    }

}
