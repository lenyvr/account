package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RegisterTransactionResponseDTO(
        Long transactionId,
        Long accountId,
        BigDecimal amount,
        Integer transactionTypeId,
        String accountNumberDestination,
        OffsetDateTime transactionDate
) {}
