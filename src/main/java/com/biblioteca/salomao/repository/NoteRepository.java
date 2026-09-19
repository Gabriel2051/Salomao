package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Optional<Note> findByIdAndUserId(UUID id, UUID userId);
    Page<Note> findByUserId(UUID userId, Pageable pageable);
    Page<Note> findByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);
    List<Note> findTop10ByUserIdOrderByUpdatedAtDesc(UUID userId);
    long countByUserId(UUID userId);
    @Query("select n from Note n where n.user.id = :uid and (lower(n.title) like lower(concat('%', :q, '%')) or lower(n.content) like lower(concat('%', :q, '%')))")
    Page<Note> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
