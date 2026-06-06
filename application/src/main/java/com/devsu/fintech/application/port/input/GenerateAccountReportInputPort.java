package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.AccountReport;
import java.time.LocalDate;

public interface GenerateAccountReportInputPort {

    AccountReport execute(String clientIdentificationNumber, LocalDate startDate, LocalDate endDate);
}
