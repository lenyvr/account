package com.devsu.fintech.domain.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long clientId) {
        super("Client not found or not verified with id: " + clientId);
    }

    public ClientNotFoundException(String identifier) {
        super("Client not found with identification: " + identifier);
    }
}
