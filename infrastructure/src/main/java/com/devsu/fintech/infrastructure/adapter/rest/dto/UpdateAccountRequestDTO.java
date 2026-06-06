package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.time.LocalDate;

public record UpdateAccountRequestDTO(
        Integer accountStatusId,
        LocalDate expiryDepositDate
) {}
