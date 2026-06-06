package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.ListAccountsInputPort;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;

public class ListAccountsUseCase implements ListAccountsInputPort {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final AccountRepositorySPI accountRepositorySPI;

    public ListAccountsUseCase(AccountRepositorySPI accountRepositorySPI) {
        this.accountRepositorySPI = accountRepositorySPI;
    }

    @Override
    public AccountPage execute(AccountFilter filter, int page, int size) {
        int resolvedSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        return accountRepositorySPI.listAccounts(filter, page, resolvedSize);
    }
}
