package com.devsu.fintech.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionReportItem(
        OffsetDateTime transactionDate,
        String transactionType,
        BigDecimal transactionAmount,
        String transactionDescription
) {}
