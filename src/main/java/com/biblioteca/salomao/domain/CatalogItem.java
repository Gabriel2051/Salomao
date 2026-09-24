package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Item do catálogo do universo narrativo: armas, trajes, feitiços, artefatos,
 * materiais (metais, gemas), relíquias, veículos — qualquer "coisa" com
 * aparência, história e função próprias. Pode estar vinculado a um personagem.
 */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "catalog_items", indexes = {
        @Index(name = "idx_catalog_user", columnList = "user_id"),
        @Index(name = "idx_catalog_user_updated", columnList = "user_id, updated_at"),
        @Index(name = "idx_catalog_user_kind", columnList = "user_id, kind"),
        @Index(name = "idx_catalog_user_name", columnList = "user_id, name")
})
public class CatalogItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_catalog_user"))
    private User user;

    @Column(nullable = false, length = 160)
    private String name = "";

    /** Tipo do item: Arma, Traje, Feitiço, Material, Artefato… (texto livre com sugestões). */
    @Column(length = 60)
    private String kind = "";

    /** Resumo curto exibido em cards e resultados de pesquisa. */
    @Column(length = 240)
    private String summary = "";

    /** Aparência do item (editor rico). */
    @Column(columnDefinition = "TEXT")
    private String appearance = "";

    /** História do item: quem forjou, por onde passou (editor rico). */
    @Column(columnDefinition = "TEXT")
    private String lore = "";

    /** O que o item faz: efeitos, poderes, utilidade (editor rico). */
    @Column(name = "effects", columnDefinition = "TEXT")
    private String effects = "";

    /** Do que é feito (metal, gema, tecido encantado…). */
    @Column(length = 160)
    private String material = "";

    /** Raridade: Comum, Raro, Lendário… (texto livre com sugestões). */
    @Column(length = 40)
    private String rarity = "";

    /** Personagem que porta/possui o item (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id",
            foreignKey = @ForeignKey(name = "fk_catalog_character"))
    private Character owner;

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
