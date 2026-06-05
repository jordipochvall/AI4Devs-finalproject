package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "operators")
public class OperatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    void touch() { updatedAt = OffsetDateTime.now(); }

    public Long getId()        { return id; }
    public String getCode()    { return code; }
    public String getName()    { return name; }
    public boolean isActive()  { return active; }
}
