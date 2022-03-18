package com.exemple.service.schema.common.exception;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonPointer;

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

    public static List<ValidationExceptionModel> buildException(org.everit.json.schema.ValidationException exception) {

        if (!exception.getCausingExceptions().isEmpty()) {

            return exception.getCausingExceptions().stream().flatMap(e -> ValidationExceptionBuilder.buildException(e).stream())
                    .collect(Collectors.toList());

        }

        return Collections.singletonList(build(exception));

    }

    private static ValidationExceptionModel build(org.everit.json.schema.ValidationException exception) {

        String path = SEPARATOR.matcher(exception.getPointerToViolation()).replaceFirst("");

        switch (exception.getKeyword()) {
            case "required":
            case "additionalProperties":
                path = path.concat("/").concat(getValue(exception.getErrorMessage()));
                break;
            default:

        }

        return new ValidationExceptionModel(JsonPointer.compile(path), exception.getKeyword(), exception.getErrorMessage());
    }

    private static String getValue(String message) {

        Matcher matcher = PATTERN.matcher(message);

        Assert.isTrue(matcher.lookingAt(), "Pattern is invalid");

        return matcher.group(1);

    }

}
