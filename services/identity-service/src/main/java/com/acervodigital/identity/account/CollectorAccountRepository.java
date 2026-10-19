package com.acervodigital.identity.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectorAccountRepository extends JpaRepository<CollectorAccount, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from CollectorAccount account where account.role = :role and account.active = true order by account.id")
    List<CollectorAccount> lockActiveAccountsByRole(@Param("role") CollectorRole role);

    List<CollectorAccount> findAllByOrderByCreatedAtDesc();

    Optional<CollectorAccount> findByEmail(String email);
}