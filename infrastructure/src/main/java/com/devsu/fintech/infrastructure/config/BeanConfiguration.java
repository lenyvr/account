package com.devsu.fintech.infrastructure.config;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.application.usecase.CreateAccountUseCase;
import com.devsu.fintech.application.usecase.HasOpenedAccountsUseCase;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.ClientVerificationSPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public HasOpenedAccountsInputPort hasOpenedAccountsUseCase(AccountRepositorySPI accountRepositorySPI) {
        return new HasOpenedAccountsUseCase(accountRepositorySPI);
    }

    @Bean
    public CreateAccountInputPort createAccountUseCase(AccountRepositorySPI accountRepositorySPI,
                                                       ClientVerificationSPI clientVerificationSPI) {
        return new CreateAccountUseCase(accountRepositorySPI, clientVerificationSPI);
    }
}
