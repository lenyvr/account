package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class AccountSpecification {

    private AccountSpecification() {}

    public static Specification<AccountEntity> excludeClosed() {
        return (root, query, cb) -> cb.notEqual(root.get("accountStatusId"), 5);
    }

    public static Specification<AccountEntity> accountNumberEquals(String accountNumber) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("accountNumber").as(String.class)), "%"+accountNumber.toLowerCase()+"%");
    }

    public static Specification<AccountEntity> typeIdEquals(Integer typeId) {
        return (root, query, cb) -> cb.equal(root.get("accountTypeId"), typeId);
    }

    public static Specification<AccountEntity> statusIdEquals(Integer statusId) {
        return (root, query, cb) -> cb.equal(root.get("accountStatusId"), statusId);
    }

    public static Specification<AccountEntity> createdOnDate(LocalDate date) {
        return (root, query, cb) -> {
            OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime end = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.and(
                    cb.greaterThanOrEqualTo(root.get("createdDate"), start),
                    cb.lessThan(root.get("createdDate"), end)
            );
        };
    }

    public static Specification<AccountEntity> balanceGreaterThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("balance"), amount);
    }

    public static Specification<AccountEntity> balanceLessThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("balance"), amount);
    }
}
