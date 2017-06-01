package com.mst.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class LocalDateTimeConverter extends TypeConverter implements SimpleValueConverter {

    public LocalDateTimeConverter() {
        // TODO: Add other date/time supported classes here
        // Other java.time classes: LocalDate.class, LocalTime.class
        // Arrays: LocalDateTime[].class, etc
        super(LocalDateTime.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) {
            return null;
        }
 
        if (fromDBObject instanceof Date) {
            return ((Date) fromDBObject).toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
        }

        if (fromDBObject instanceof LocalDateTime) {
            return fromDBObject;
        }

        // TODO: decode other types

        throw new IllegalArgumentException(String.format("Cannot decode object of class: %s", fromDBObject.getClass().getName()));
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date) {
            return value;
        }

        if (value instanceof LocalDateTime) {
            ZonedDateTime zoned = ((LocalDateTime) value).atZone(ZoneOffset.systemDefault());
            return Date.from(zoned.toInstant());
        }

        // TODO: encode other types

        throw new IllegalArgumentException(String.format("Cannot encode object of class: %s", value.getClass().getName()));
    }
    

}