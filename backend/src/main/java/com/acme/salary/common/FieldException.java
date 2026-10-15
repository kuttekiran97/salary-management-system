package com.acme.salary.common;

/**
 * A request was rejected because of one field, so the UI can show the message next to it.
 * {@link Kind#INVALID} is returned as 400, {@link Kind#CONFLICT} (e.g. duplicate email) as 409.
 */
public class FieldException extends RuntimeException {

    public enum Kind { INVALID, CONFLICT }

    private final Kind kind;
    private final String field;

    private FieldException(Kind kind, String field, String message) {
        super(message);
        this.kind = kind;
        this.field = field;
    }

    public static FieldException invalid(String field, String message) {
        return new FieldException(Kind.INVALID, field, message);
    }

    public static FieldException conflict(String field, String message) {
        return new FieldException(Kind.CONFLICT, field, message);
    }

    public Kind getKind() {
        return kind;
    }

    public String getField() {
        return field;
    }
}
