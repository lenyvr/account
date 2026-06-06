package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DeactivateAccountResponseDTO(
        Long accountId,
        String accountNumber,
        Integer accountStatusId,
        BigDecimal balance,
        BigDecimal refundedAmount,
        OffsetDateTime lastStatusDate
) {}
