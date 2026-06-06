package com.devsu.fintech.domain.exception;

public class InvalidDormantTransitionException extends RuntimeException {

    public InvalidDormantTransitionException() {
        super("Account cannot be set to DORMANT: it must have a zero balance and no activity for at least 6 months");
    }
}
