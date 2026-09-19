package com.biblioteca.salomao.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "mental_map_edges", indexes = {
        @Index(name = "idx_edges_map", columnList = "map_id")
})
public class MindMapEdge {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_edges_map"))
    private MentalMap map;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_edges_source"))
    private MindMapNode source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_edges_target"))
    private MindMapNode target;

    @Column(length = 120)
    private String label = "";
}
