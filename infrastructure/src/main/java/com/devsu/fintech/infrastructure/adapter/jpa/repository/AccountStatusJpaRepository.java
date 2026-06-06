package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusJpaRepository extends JpaRepository<AccountStatusEntity, Integer> {}
