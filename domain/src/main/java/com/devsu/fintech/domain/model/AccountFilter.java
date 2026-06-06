package com.devsu.fintech.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountFilter(
        String accountNumber,
        String accountType,
        LocalDate createdDate,
        String accountStatus,
        BigDecimal initialBalance,
        BigDecimal finalBalance
) {}
