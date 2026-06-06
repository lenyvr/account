package com.devsu.fintech.domain.ports.output;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Integer clientId);
}
