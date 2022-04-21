package com.exemple.service.schema.validation.custom;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class CustomDateTimeFormatValidator implements FormatValidator {

    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME;
    static {
        ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .optionalStart()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .optionalEnd()
                .optionalStart()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .optionalEnd()
                .appendOffsetId()
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            ISO_OFFSET_DATE_TIME.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid %s. %s", subject, formatName(), e.getMessage()));
        }
    }

    @Override
    public String formatName() {
        return "date-time";
    }

}
