package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidIpAddressException extends ResponseStatusException {

    public InvalidIpAddressException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public InvalidIpAddressException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
