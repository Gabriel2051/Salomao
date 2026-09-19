package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Referencia estruturada personagem <-> historia (mencao @).
 * Nao depende apenas do texto: e uma linha de relacionamento real no banco.
 */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "story_character_mentions", indexes = {
        @Index(name = "idx_mentions_story", columnList = "story_id"),
        @Index(name = "idx_mentions_character", columnList = "character_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_story_character", columnNames = {"story_id", "character_id"})
})
public class StoryMention {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mentions_story"))
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mentions_character"))
    private Character character;
}
