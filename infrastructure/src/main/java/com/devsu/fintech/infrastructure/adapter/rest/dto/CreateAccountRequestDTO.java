package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAccountRequestDTO(
        String accountNumber,
        BigDecimal balance,
        Long clientId,
        Integer accountTypeId,
        LocalDate expiryDepositDate
) {}
