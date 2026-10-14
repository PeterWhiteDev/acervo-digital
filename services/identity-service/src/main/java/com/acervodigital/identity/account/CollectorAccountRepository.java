package com.acervodigital.identity.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectorAccountRepository extends JpaRepository<CollectorAccount, UUID> {

    boolean existsByEmail(String email);

    Optional<CollectorAccount> findByEmail(String email);
}