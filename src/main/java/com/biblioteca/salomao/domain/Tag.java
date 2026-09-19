package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Tag global do usuario (reutilizavel entre entidades). */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "tags",
        uniqueConstraints = @UniqueConstraint(name = "uq_tags_user_name", columnNames = {"user_id", "name"}),
        indexes = @Index(name = "idx_tags_user", columnList = "user_id"))
public class Tag {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tags_user"))
    private User user;

    @Column(nullable = false, length = 40)
    private String name = "";

    @Column(length = 20)
    private String color = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }
}
