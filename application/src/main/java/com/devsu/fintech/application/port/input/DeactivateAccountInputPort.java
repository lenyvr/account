package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.DeactivationResult;

public interface DeactivateAccountInputPort {

    DeactivationResult execute(String accountNumber, String refundMethod, String targetAccountNumber);
}
