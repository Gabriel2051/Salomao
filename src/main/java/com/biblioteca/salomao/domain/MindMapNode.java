package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "mental_map_nodes", indexes = {
        @Index(name = "idx_nodes_map", columnList = "map_id")
})
public class MindMapNode {

    public enum NodeType { TEXTO, PERSONAGEM, PODER, HISTORIA, ANOTACAO, CATEGORIA, ITEM, LIVRE }

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_nodes_map"))
    private MentalMap map;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    private NodeType nodeType = NodeType.TEXTO;

    @Column(nullable = false, length = 200)
    private String label = "";

    @Column(columnDefinition = "TEXT")
    private String content = "";

    /** Tipo da entidade referenciada (CHARACTER, POWER, STORY, NOTE) ou null. */
    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "pos_x", nullable = false)
    private double posX = 0;

    @Column(name = "pos_y", nullable = false)
    private double posY = 0;

    @Column(length = 20)
    private String color = "";

    @Column(nullable = false)
    private boolean collapsed = false;
}
