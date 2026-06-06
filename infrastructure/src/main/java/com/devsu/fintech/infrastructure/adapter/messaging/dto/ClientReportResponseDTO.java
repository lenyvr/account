package com.devsu.fintech.infrastructure.adapter.messaging.dto;

public record ClientReportResponseDTO(
        Long clientId,
        String firstName,
        String lastName,
        String identificationNumber,
        String identificationType,
        String address,
        String email,
        String contactNumber
) {}
