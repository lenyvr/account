package com.devsu.fintech.domain.exception;

public class AccountClosedException extends RuntimeException {

    public AccountClosedException(String accountNumber) {
        super("Account " + accountNumber + " is closed and cannot be modified");
    }
}
