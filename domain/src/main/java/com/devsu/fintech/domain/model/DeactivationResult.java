package com.devsu.fintech.domain.model;

import java.math.BigDecimal;

public record DeactivationResult(Account account, BigDecimal refundedAmount) {}
