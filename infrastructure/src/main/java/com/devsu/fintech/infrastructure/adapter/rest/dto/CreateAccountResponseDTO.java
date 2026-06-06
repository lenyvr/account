package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CreateAccountResponseDTO(
        Long accountId,
        String accountNumber,
        BigDecimal initialAmount,
        BigDecimal balance,
        Integer accountStatusId,
        Long clientId,
        Integer accountTypeId,
        LocalDate expiryDepositDate,
        OffsetDateTime createdDate
) {}
