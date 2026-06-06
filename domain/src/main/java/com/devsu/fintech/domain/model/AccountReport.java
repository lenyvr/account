package com.devsu.fintech.domain.model;

import java.util.List;

public record AccountReport(
        String clientName,
        String clientIdentificationNumber,
        String clientContactNumber,
        String clientEmail,
        List<AccountReportItem> accounts
) {}
