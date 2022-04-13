package com.exemple.service.schema.common.exception;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationExceptionBuilder {

    private static final Pattern PATTERN;

    private static final Pattern SEPARATOR;

    static {

        PATTERN = Pattern.compile(".*\\[(.*)\\].*");
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

        switch (exception.getKeyword()) {
            case "required":
            case "additionalProperties":
                path = path.concat("/").concat(getValue(exception.getErrorMessage()));
                break;
            default:

        }

        JsonNode value = source.at(path);
        if (value.isMissingNode() && !JsonPointer.compile(path).head().toString().isEmpty()) {
            value = source.at(JsonPointer.compile(path).head());
        }

        return new ValidationExceptionCause(JsonPointer.compile(path), exception.getKeyword(), exception.getErrorMessage(), value);
    }

    private static String getValue(String message) {

        Matcher matcher = PATTERN.matcher(message);

        Assert.isTrue(matcher.lookingAt(), "Pattern is invalid");

        return matcher.group(1);

    }

}
