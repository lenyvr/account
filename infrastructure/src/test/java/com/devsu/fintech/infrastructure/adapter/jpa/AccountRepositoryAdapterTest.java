package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountStatusJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "spring.sql.init.separator=@@",
        "spring.jpa.defer-datasource-initialization=true"
})
class AccountRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private AccountTypeJpaRepository accountTypeRepository;

    @Autowired
    private AccountStatusJpaRepository accountStatusRepository;

    private AccountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountRepositoryAdapter(accountJpaRepository, accountTypeRepository, accountStatusRepository);
    }

    @Test
    void shouldReturnTrueWhenClientHasActiveAccount() {
        accountJpaRepository.save(buildAccount("ACC-001", 1L, 2)); // ACTIVE

        assertTrue(adapter.hasOpenedAccounts(1L));
    }

    @Test
    void shouldReturnTrueWhenClientHasNonClosedAccountAmongMany() {
        accountJpaRepository.save(buildAccount("ACC-002", 2L, 5)); // CLOSED
        accountJpaRepository.save(buildAccount("ACC-003", 2L, 3)); // BLOCKED

        assertTrue(adapter.hasOpenedAccounts(2L));
    }

    @Test
    void shouldReturnFalseWhenClientHasOnlyClosedAccounts() {
        accountJpaRepository.save(buildAccount("ACC-004", 3L, 5)); // CLOSED
        accountJpaRepository.save(buildAccount("ACC-005", 3L, 5)); // CLOSED

        assertFalse(adapter.hasOpenedAccounts(3L));
    }

    @Test
    void shouldReturnFalseWhenClientHasNoAccounts() {
        assertFalse(adapter.hasOpenedAccounts(999L));
    }

    private AccountEntity buildAccount(String accountNumber, Long clientId, Integer statusId) {
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        account.setClientId(clientId);
        account.setAccountStatusId(statusId);
        account.setAccountTypeId(1); // SAVINGS
        account.setInitialAmount(BigDecimal.ZERO);
        account.setBalance(BigDecimal.ZERO);
        return account;
    }
}
