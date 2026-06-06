package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.ClientDetails;
import java.util.Optional;

public interface ClientReportSPI {

    Optional<ClientDetails> fetchClientDetails(String identificationNumber);
}
