package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "characters", indexes = {
        @Index(name = "idx_characters_user", columnList = "user_id"),
        @Index(name = "idx_characters_user_updated", columnList = "user_id, updated_at"),
        @Index(name = "idx_characters_user_name", columnList = "user_id, full_name")
})
public class Character {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_characters_user"))
    private User user;

    // --- informacoes basicas ---
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName = "";

    @Column(length = 160)
    private String alias = "";

    @Column(length = 80)
    private String nickname = "";

    private Integer age;

    @Column(name = "birth_date", length = 40)
    private String birthDate = "";

    @Column(length = 40)
    private String gender = "";

    @Column(length = 80)
    private String species = "";

    @Column(length = 80)
    private String nationality = "";

    @Column(length = 120)
    private String occupation = "";

    @Column(length = 40)
    private String status = "";

    // --- aparencia ---
    @Column(name = "physical_desc", columnDefinition = "TEXT")
    private String physicalDesc = "";

    @Column(length = 20)
    private String height = "";

    @Column(length = 20)
    private String weight = "";

    @Column(name = "eye_color", length = 40)
    private String eyeColor = "";

    @Column(name = "hair_color", length = 40)
    private String hairColor = "";

    @Column(name = "special_traits", columnDefinition = "TEXT")
    private String specialTraits = "";

    @Column(columnDefinition = "TEXT")
    private String clothing = "";

    @Column(columnDefinition = "TEXT")
    private String marks = "";

    @Column(columnDefinition = "TEXT")
    private String scars = "";

    @Column(name = "other_details", columnDefinition = "TEXT")
    private String otherDetails = "";

    // --- personalidade ---
    @Column(columnDefinition = "TEXT")
    private String personality = "";

    @Column(columnDefinition = "TEXT")
    private String likes = "";

    @Column(columnDefinition = "TEXT")
    private String dislikes = "";

    @Column(columnDefinition = "TEXT")
    private String fears = "";

    @Column(columnDefinition = "TEXT")
    private String goals = "";

    @Column(columnDefinition = "TEXT")
    private String motivations = "";

    @Column(columnDefinition = "TEXT")
    private String weaknesses = "";

    @Column(columnDefinition = "TEXT")
    private String virtues = "";

    @Column(columnDefinition = "TEXT")
    private String flaws = "";

    // --- historia / habilidades ---
    @Column(columnDefinition = "TEXT")
    private String backstory = "";

    @Column(columnDefinition = "TEXT")
    private String skills = "";

    @Column(name = "tags_csv", length = 500)
    private String tagsCsv = "";

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(nullable = false)
    private boolean archived = false;

    @ManyToMany
    @JoinTable(name = "character_powers",
            joinColumns = @JoinColumn(name = "character_id",
                    foreignKey = @ForeignKey(name = "fk_charpowers_char")),
            inverseJoinColumns = @JoinColumn(name = "power_id",
                    foreignKey = @ForeignKey(name = "fk_charpowers_power")),
            uniqueConstraints = @UniqueConstraint(name = "uq_character_power",
                    columnNames = {"character_id", "power_id"}))
    private Set<Power> powers = new HashSet<>();

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
