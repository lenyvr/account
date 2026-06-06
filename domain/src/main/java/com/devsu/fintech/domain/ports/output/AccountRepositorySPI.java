package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Integer clientId);

    Account save(Account account);
}
