package com.devsu.fintech.infrastructure.config;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.application.port.input.ListAccountsInputPort;
import com.devsu.fintech.application.port.input.UpdateAccountInputPort;
import com.devsu.fintech.application.usecase.CreateAccountUseCase;
import com.devsu.fintech.application.usecase.HasOpenedAccountsUseCase;
import com.devsu.fintech.application.usecase.ListAccountsUseCase;
import com.devsu.fintech.application.usecase.UpdateAccountUseCase;
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

    @Bean
    public UpdateAccountInputPort updateAccountUseCase(AccountRepositorySPI accountRepositorySPI) {
        return new UpdateAccountUseCase(accountRepositorySPI);
    }

    @Bean
    public ListAccountsInputPort listAccountsUseCase(AccountRepositorySPI accountRepositorySPI) {
        return new ListAccountsUseCase(accountRepositorySPI);
    }
}
