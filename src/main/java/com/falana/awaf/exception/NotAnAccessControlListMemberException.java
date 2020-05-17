package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotAnAccessControlListMemberException extends ResponseStatusException {

    public NotAnAccessControlListMemberException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotAnAccessControlListMemberException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
    }
}
