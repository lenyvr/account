package com.devsu.fintech.domain.exception;

public class TargetAccountRequiredException extends RuntimeException {

    public TargetAccountRequiredException() {
        super("A target account number is required when the refund method is 'transfer'");
    }
}
