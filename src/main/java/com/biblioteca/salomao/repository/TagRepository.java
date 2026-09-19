package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByUserIdOrderByNameAsc(UUID userId);
    Optional<Tag> findByUserIdAndNameIgnoreCase(UUID userId, String name);
}
