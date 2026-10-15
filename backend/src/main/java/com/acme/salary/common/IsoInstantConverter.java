package com.acme.salary.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;

/** Stores timestamps as ISO-8601 UTC text; SQLite has no timestamp type. */
@Converter
public class IsoInstantConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String text) {
        return text == null ? null : Instant.parse(text);
    }
}
