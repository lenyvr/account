package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.Account;

public interface CreateAccountInputPort {

    Account execute(Account account);
}
