package com.devsu.fintech.infrastructure.adapter.rest.dto;

import java.util.List;

public record PagedResponseDTO<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}
