package com.exemple.service.schema.validation.custom;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class CustomDateTimeFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid %s.", subject, formatName()));
        }
    }

    @Override
    public String formatName() {
        return "date-time";
    }

}
