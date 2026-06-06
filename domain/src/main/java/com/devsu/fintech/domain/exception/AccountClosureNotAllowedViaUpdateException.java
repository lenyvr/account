package com.devsu.fintech.domain.exception;

public class AccountClosureNotAllowedViaUpdateException extends RuntimeException {

    public AccountClosureNotAllowedViaUpdateException() {
        super("Account closure must be handled through the deactivate account endpoint");
    }
}
