package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BlacklistedException extends ResponseStatusException {

    public BlacklistedException() {
        this("IP address rejected.");
    }

    public BlacklistedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
