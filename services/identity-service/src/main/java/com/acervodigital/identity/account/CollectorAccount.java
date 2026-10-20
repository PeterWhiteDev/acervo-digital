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

    @Column(nullable = false)
    private boolean active = true;

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

    public void update(String email, String displayName, CollectorRole role, boolean active) {
        this.email = email;
        this.profile.updateDisplayName(displayName);
        this.role = role;
        this.active = active;
    }

    public void resetPassword(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public boolean isActive() {
        return active;
    }

    public CollectorProfile getProfile() {
        return profile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}