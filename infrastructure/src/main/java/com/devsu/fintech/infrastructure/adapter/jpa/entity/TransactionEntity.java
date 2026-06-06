package com.devsu.fintech.infrastructure.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Generated(event = EventType.INSERT)
    @Column(name = "transaction_date", insertable = false, updatable = false)
    private OffsetDateTime transactionDate;

    @Setter
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Setter
    @Column(name = "account_number_destination")
    private String accountNumberDestination;

    @Setter
    @Column(name = "transaction_type_id", nullable = false)
    private Integer transactionTypeId;

    @Setter
    @Column(name = "account_id")
    private Long accountId;

}
