package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AccountListItemDTO(
        Long accountId,
        String accountNumber,
        BigDecimal initialAmount,
        BigDecimal balance,
        String accountStatus,
        Long clientId,
        String accountType,
        LocalDate expiryDepositDate,
        OffsetDateTime createdDate
) {}
