package com.devsu.fintech.domain.exception;

public class InvalidRefundMethodException extends RuntimeException {

    public InvalidRefundMethodException(String value) {
        super("Invalid refund method: '" + value + "'. Allowed values: 'withdrawal', 'transfer'");
    }
}
