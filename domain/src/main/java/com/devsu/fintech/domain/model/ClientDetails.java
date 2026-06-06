package com.devsu.fintech.domain.model;

public record ClientDetails(
        Long clientId,
        String firstName,
        String lastName,
        String identificationNumber,
        String identificationType,
        String address,
        String email,
        String contactNumber
) {}
