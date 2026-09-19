package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.MindMapEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface MindMapEdgeRepository extends JpaRepository<MindMapEdge, UUID> {
    List<MindMapEdge> findByMapIdAndMapUserId(UUID mapId, UUID userId);
    void deleteByMapIdAndMapUserId(UUID mapId, UUID userId);
}
