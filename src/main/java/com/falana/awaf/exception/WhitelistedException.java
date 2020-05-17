package com.falana.awaf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WhitelistedException extends ResponseStatusException {

    public WhitelistedException() {
        this("IP address rejected.");
    }

    public WhitelistedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}

