package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "powers", indexes = {
        @Index(name = "idx_powers_user", columnList = "user_id"),
        @Index(name = "idx_powers_user_name", columnList = "user_id, name")
})
public class Power {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_powers_user"))
    private User user;

    @Column(nullable = false, length = 160)
    private String name = "";

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Column(length = 80)
    private String category = "";

    @Column(length = 40)
    private String level = "";

    @Column(columnDefinition = "TEXT")
    private String limitations = "";

    @Column(columnDefinition = "TEXT")
    private String weaknesses = "";

    @Column(columnDefinition = "TEXT")
    private String origin = "";

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
