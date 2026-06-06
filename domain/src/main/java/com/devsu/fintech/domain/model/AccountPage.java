package com.devsu.fintech.domain.model;

import java.util.List;

public class AccountPage {

    private final List<Account> accounts;
    private final long totalElements;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;

    public AccountPage(List<Account> accounts, long totalElements,
                       int totalPages, int currentPage, int pageSize) {
        this.accounts = accounts;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public List<Account> getAccounts() { return accounts; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
}
