package com.devsu.fintech.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record AccountReportItem(
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String accountStatus,
        List<TransactionReportItem> transactions
) {}
