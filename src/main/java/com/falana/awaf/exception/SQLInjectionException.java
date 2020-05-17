package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SQLInjectionException extends ResponseStatusException {

    public SQLInjectionException(String message) {
        super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, message);
    }
}
