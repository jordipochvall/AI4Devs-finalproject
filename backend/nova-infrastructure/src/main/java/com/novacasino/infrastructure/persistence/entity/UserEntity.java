package com.novacasino.infrastructure.persistence.entity;

import com.novacasino.domain.user.UserRole;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 2)
    private String locale = "es";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    void touch() { updatedAt = OffsetDateTime.now(); }

    // --- Getters ---
    public Long getId()             { return id; }
    public Long getOperatorId()     { return operatorId; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole()       { return role; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getLocale()       { return locale; }
    public boolean isActive()       { return active; }

    // --- Setters ---
    public void setOperatorId(Long operatorId)         { this.operatorId = operatorId; }
    public void setEmail(String email)                 { this.email = email; }
    public void setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }
    public void setRole(UserRole role)                 { this.role = role; }
    public void setBirthDate(LocalDate birthDate)      { this.birthDate = birthDate; }
    public void setLocale(String locale)               { this.locale = locale; }
    public void setActive(boolean active)              { this.active = active; }
}
