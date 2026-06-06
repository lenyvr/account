package com.devsu.fintech.domain.model;

import java.util.Optional;

public enum RefundMethod {
    WITHDRAWAL, TRANSFER;

    public static Optional<RefundMethod> fromString(String value) {
        if (value == null) return Optional.empty();
        return switch (value.trim().toLowerCase()) {
            case "withdrawal" -> Optional.of(WITHDRAWAL);
            case "transfer"   -> Optional.of(TRANSFER);
            default           -> Optional.empty();
        };
    }
}
