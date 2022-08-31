package com.exemple.service.schema.common.exception;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationExceptionBuilder {

    private static final Pattern ADDITIONAL_PROPERTIES_PATTERN;

    private static final Pattern REQUIRED_PATTERN;

    private static final Pattern SEPARATOR;

    static {

        ADDITIONAL_PROPERTIES_PATTERN = Pattern.compile("extraneous key \\[(.*)\\] is not permitted");
        REQUIRED_PATTERN = Pattern.compile("required key \\[(.*)\\] not found");
        SEPARATOR = Pattern.compile("#");
    }

    public static Set<ValidationExceptionCause> buildException(org.everit.json.schema.ValidationException exception, JsonNode source) {

        if (!exception.getCausingExceptions().isEmpty()) {

            return exception.getCausingExceptions().stream().flatMap(e -> ValidationExceptionBuilder.buildException(e, source).stream())
                    .collect(Collectors.toSet());

        }

        return Collections.singleton(build(exception, source));

    }

    private static ValidationExceptionCause build(org.everit.json.schema.ValidationException exception, JsonNode source) {

        String path = SEPARATOR.matcher(exception.getPointerToViolation()).replaceFirst("");

        path = switch (exception.getKeyword()) {
            case "required" -> path.concat("/").concat(extractField(REQUIRED_PATTERN, exception));
            case "additionalProperties" -> path.concat("/").concat(extractField(ADDITIONAL_PROPERTIES_PATTERN, exception));
            default -> path;
        };

        JsonNode value = source.at(path);
        if (value.isMissingNode() && !JsonPointer.compile(path).head().toString().isEmpty()) {
            value = source.at(JsonPointer.compile(path).head());
        }

        return new ValidationExceptionCause(JsonPointer.compile(path), exception.getKeyword(), exception.getErrorMessage(), value);
    }

    private static String extractField(Pattern pattern, org.everit.json.schema.ValidationException exception) {

        var matcher = pattern.matcher(exception.getErrorMessage());

        Assert.isTrue(matcher.lookingAt(), "Pattern is invalid");

        return matcher.group(1);

    }

}
