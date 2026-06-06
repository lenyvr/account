package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.Account;
import java.time.LocalDate;

public interface UpdateAccountInputPort {

    Account execute(String accountNumber, Integer newStatusId, LocalDate newExpiryDate);
}
