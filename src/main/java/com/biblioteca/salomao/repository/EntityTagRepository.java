package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.EntityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface EntityTagRepository extends JpaRepository<EntityTag, UUID> {
    List<EntityTag> findByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);
    void deleteByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);
}
