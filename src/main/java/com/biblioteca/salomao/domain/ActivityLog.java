package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Registro de atividades para o dashboard ("Atividades recentes"). */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "activity_log", indexes = {
        @Index(name = "idx_activity_user", columnList = "user_id, created_at")
})
public class ActivityLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_activity_user"))
    private User user;

    @Column(nullable = false, length = 40)
    private String action = "";

    @Column(name = "entity_type", length = 20)
    private String entityType = "";

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(length = 300)
    private String description = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }
}
