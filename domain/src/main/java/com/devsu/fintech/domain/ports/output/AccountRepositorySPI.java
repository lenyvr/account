package com.devsu.fintech.domain.ports.output;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Long clientId);
}
