package com.acervodigital.identity.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "collector_account")
public class CollectorAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectorRole role;

    @Embedded
    private CollectorProfile profile;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CollectorAccount() {
    }

    private CollectorAccount(String email, String passwordHash, CollectorProfile profile) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = CollectorRole.USER;
        this.profile = profile;
    }

    public static CollectorAccount register(String email, String passwordHash, String displayName) {
        return new CollectorAccount(email, passwordHash, CollectorProfile.create(displayName));
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public CollectorRole getRole() {
        return role;
    }

    public CollectorProfile getProfile() {
        return profile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}