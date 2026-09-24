package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "stories", indexes = {
        @Index(name = "idx_stories_user", columnList = "user_id"),
        @Index(name = "idx_stories_user_updated", columnList = "user_id, updated_at"),
        @Index(name = "idx_stories_user_title", columnList = "user_id, title")
})
public class Story {

    public enum Status {
        RASCUNHO("Rascunho"), EM_REVISAO("Em revisão"), CONCLUIDA("Concluída");
        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stories_user"))
    private User user;

    @Column(nullable = false, length = 220)
    private String title = "";

    @Column(length = 300)
    private String subtitle = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content = "";

    @Column(name = "tags_csv", length = 500)
    private String tagsCsv = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.RASCUNHO;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(nullable = false)
    private boolean archived = false;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryMention> mentions = new ArrayList<>();

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
