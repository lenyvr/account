package com.devsu.fintech.domain.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Transaction rejected: insufficient funds to complete this operation");
    }
}
