package com.exemple.service.schema.core.validator;

import com.exemple.service.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;

public interface ValidatorService {

    void validate(String path, JsonNode form, JsonNode old, ValidationException validationException);
}
