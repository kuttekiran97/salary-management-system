package com.acme.salary.common;

/** The requested resource does not exist. Returned as 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
