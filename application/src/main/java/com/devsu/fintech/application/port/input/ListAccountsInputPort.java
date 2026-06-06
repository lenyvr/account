package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;

public interface ListAccountsInputPort {

    AccountPage execute(AccountFilter filter, int page, int size);
}
