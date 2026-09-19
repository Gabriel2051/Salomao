package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface StoryRepository extends JpaRepository<Story, UUID> {
    Optional<Story> findByIdAndUserId(UUID id, UUID userId);
    Page<Story> findByUserId(UUID userId, Pageable pageable);
    Page<Story> findByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);
    List<Story> findTop10ByUserIdOrderByUpdatedAtDesc(UUID userId);
    @Query("select s from Story s where s.user.id = :uid and (lower(s.title) like lower(concat('%', :q, '%')) or lower(s.content) like lower(concat('%', :q, '%')))")
    Page<Story> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    long countByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
