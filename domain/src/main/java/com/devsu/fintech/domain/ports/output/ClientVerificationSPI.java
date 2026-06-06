package com.devsu.fintech.domain.ports.output;

public interface ClientVerificationSPI {

    boolean existsClient(Long clientId);
}
