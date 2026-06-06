package com.devsu.fintech.infrastructure.adapter.rest.dto;

import com.devsu.fintech.domain.model.RefundMethod;

public record DeactivateAccountRequestDTO(
        String refundMethod,
        String targetAccountNumber
) {}
