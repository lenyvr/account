package com.devsu.fintech.infrastructure.adapter.messaging.dto;

public record CheckAccountResponseDTO(Long clientId, boolean hasOpenAccounts) {
}
