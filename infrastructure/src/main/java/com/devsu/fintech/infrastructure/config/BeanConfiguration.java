package com.devsu.fintech.infrastructure.config;

import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.application.usecase.HasOpenedAccountsUseCase;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public HasOpenedAccountsInputPort hasOpenedAccountsUseCase(AccountRepositorySPI accountRepositorySPI) {
        return new HasOpenedAccountsUseCase(accountRepositorySPI);
    }
}
