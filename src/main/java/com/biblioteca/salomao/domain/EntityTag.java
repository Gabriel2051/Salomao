package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Vinculo polimorfico tag <-> entidade (NOTE, CHARACTER, POWER, STORY, MENTAL_MAP). */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "entity_tags",
        uniqueConstraints = @UniqueConstraint(name = "uq_entity_tag",
                columnNames = {"user_id", "entity_type", "entity_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_entitytags_user", columnList = "user_id"),
                @Index(name = "idx_entitytags_lookup", columnList = "entity_type, entity_id")
        })
public class EntityTag {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_entitytags_user"))
    private User user;

    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType = "";

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_entitytags_tag"))
    private Tag tag;
}
