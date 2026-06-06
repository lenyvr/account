package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.GenerateAccountReportInputPort;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountReport;
import com.devsu.fintech.domain.model.AccountReportItem;
import com.devsu.fintech.domain.model.ClientDetails;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.model.TransactionReportItem;
import com.devsu.fintech.domain.model.TransactionType;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.ClientReportSPI;
import com.devsu.fintech.domain.ports.output.TransactionRepositorySPI;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateAccountReportUseCase implements GenerateAccountReportInputPort {

    private final ClientReportSPI clientReportSPI;
    private final AccountRepositorySPI accountRepositorySPI;
    private final TransactionRepositorySPI transactionRepositorySPI;

    public GenerateAccountReportUseCase(ClientReportSPI clientReportSPI,
                                        AccountRepositorySPI accountRepositorySPI,
                                        TransactionRepositorySPI transactionRepositorySPI) {
        this.clientReportSPI = clientReportSPI;
        this.accountRepositorySPI = accountRepositorySPI;
        this.transactionRepositorySPI = transactionRepositorySPI;
    }

    @Override
    public AccountReport execute(String clientIdentificationNumber, LocalDate startDate, LocalDate endDate) {
        ClientDetails client = clientReportSPI.fetchClientDetails(clientIdentificationNumber)
                .orElseThrow(() -> new ClientNotFoundException(clientIdentificationNumber));

        List<Account> accounts = accountRepositorySPI.findByClientId(client.clientId());

        List<Long> accountIds = accounts.stream().map(Account::getAccountId).toList();

        OffsetDateTime from = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Transaction> transactions = accountIds.isEmpty()
                ? List.of()
                : transactionRepositorySPI.findTransactionsForReport(accountIds, from, to);

        Map<Long, List<Transaction>> txByAccountId = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getAccountId));

        List<AccountReportItem> reportItems = accounts.stream()
                .map(account -> buildAccountReportItem(account, txByAccountId.getOrDefault(account.getAccountId(), List.of())))
                .toList();

        return new AccountReport(
                client.firstName() + " " + client.lastName(),
                client.identificationNumber(),
                client.contactNumber(),
                client.email(),
                reportItems
        );
    }

    private AccountReportItem buildAccountReportItem(Account account, List<Transaction> transactions) {
        List<TransactionReportItem> txItems = transactions.stream()
                .map(this::toTransactionReportItem)
                .toList();

        return new AccountReportItem(
                account.getAccountNumber(),
                account.getAccountTypeName(),
                account.getBalance(),
                account.getAccountStatusName(),
                txItems
        );
    }

    private TransactionReportItem toTransactionReportItem(Transaction tx) {
        String typeName = resolveTypeName(tx.getTransactionTypeId());
        return new TransactionReportItem(
                tx.getTransactionDate(),
                typeName,
                tx.getAmount(),
                typeName
        );
    }

    private String resolveTypeName(Integer typeId) {
        return Arrays.stream(TransactionType.values())
                .filter(t -> t.getId().equals(typeId))
                .map(TransactionType::name)
                .findFirst()
                .orElse("UNKNOWN");
    }
}
