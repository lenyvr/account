package com.devsu.fintech.infrastructure.adapter.rest.dto;

import com.devsu.fintech.domain.model.TransactionType;

import java.math.BigDecimal;

public record RegisterTransactionRequestDTO(
        Long accountId,
        BigDecimal amount,
        TransactionType transactionType,
        String accountNumberDestination
) {}
