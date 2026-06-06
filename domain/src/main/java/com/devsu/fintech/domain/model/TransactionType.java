package com.devsu.fintech.domain.model;

public enum TransactionType {


    CASH_DEPOSIT("deposit",1),
    TRANSFER_INBOUND("transfer_inbound", 2),
    CASH_WITHDRAWAL("withdrawal", 3),
    TRANSFER_OUTBOUND("transfer_outbound", 4),;

    private final String name;
    private final Integer id;

    TransactionType(String name, Integer id) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
