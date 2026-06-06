package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;
import java.util.Optional;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Long clientId);

    Account save(Account account);

    Optional<Account> findByAccountNumber(String accountNumber);

    Account update(Account account);
}
