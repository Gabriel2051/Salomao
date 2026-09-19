package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.MindMapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface MindMapNodeRepository extends JpaRepository<MindMapNode, UUID> {
    List<MindMapNode> findByMapIdAndMapUserId(UUID mapId, UUID userId);
    Optional<MindMapNode> findByIdAndMapUserId(UUID id, UUID userId);
    void deleteByMapIdAndMapUserId(UUID mapId, UUID userId);
    List<MindMapNode> findByMapUserIdAndRefTypeAndRefId(UUID userId, String refType, UUID refId);
}
