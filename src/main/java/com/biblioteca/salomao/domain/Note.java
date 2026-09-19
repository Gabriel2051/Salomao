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
@Table(name = "notes", indexes = {
        @Index(name = "idx_notes_user", columnList = "user_id"),
        @Index(name = "idx_notes_user_updated", columnList = "user_id, updated_at"),
        @Index(name = "idx_notes_user_title", columnList = "user_id, title")
})
public class Note {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notes_user"))
    private User user;

    @Column(nullable = false, length = 200)
    private String title = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content = "";

    @Column(length = 80)
    private String category = "";

    @Column(name = "tags_csv", length = 500)
    private String tagsCsv = "";

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(nullable = false)
    private boolean archived = false;

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
