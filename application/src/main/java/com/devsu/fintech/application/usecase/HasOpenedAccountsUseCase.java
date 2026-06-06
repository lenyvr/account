package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;

public class HasOpenedAccountsUseCase implements HasOpenedAccountsInputPort {

    private final AccountRepositorySPI accountRepositorySPI;

    public HasOpenedAccountsUseCase(AccountRepositorySPI accountRepositorySPI) {
        this.accountRepositorySPI = accountRepositorySPI;
    }

    @Override
    public boolean execute(Long clientId) {
        return accountRepositorySPI.hasOpenedAccounts(clientId);
    }
}
