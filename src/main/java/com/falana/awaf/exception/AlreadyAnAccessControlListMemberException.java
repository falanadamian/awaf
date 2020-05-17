package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyAnAccessControlListMemberException extends ResponseStatusException {

    public AlreadyAnAccessControlListMemberException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public AlreadyAnAccessControlListMemberException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, message, cause);
    }
}
