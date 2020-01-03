package com.exemple.service.schema.common.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;

public final class ValidationExceptionBuilder {

    private static final Pattern PATTERN;

    private static final Pattern SEPARATOR;

    private ValidationExceptionBuilder() {

    }

    static {

        PATTERN = Pattern.compile(".*\\[(.*)\\].*");
        SEPARATOR = Pattern.compile("#");
    }

    public static void buildException(org.everit.json.schema.ValidationException exception, ValidationException validationException) {

        if (!exception.getCausingExceptions().isEmpty()) {

            exception.getCausingExceptions().forEach(e -> buildException(e, validationException));

        } else {

            String path = SEPARATOR.matcher(exception.getPointerToViolation()).replaceFirst("");

            switch (exception.getKeyword()) {
                case "required":
                case "additionalProperties":
                    path = path.concat("/").concat(getValue(exception.getErrorMessage()));
                    break;
                default:

            }

            validationException.add(new ValidationExceptionModel(path, exception.getKeyword(), exception.getErrorMessage()));
        }
    }

    private static String getValue(String message) {

        Matcher matcher = PATTERN.matcher(message);

        Assert.isTrue(matcher.lookingAt(), "Pattern is invalid");

        return matcher.group(1);

    }

}
