package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import java.util.Optional;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Long clientId);

    Account save(Account account);

    Optional<Account> findByAccountNumber(String accountNumber);

    Account update(Account account);

    AccountPage listAccounts(AccountFilter filter, int page, int size);
}
