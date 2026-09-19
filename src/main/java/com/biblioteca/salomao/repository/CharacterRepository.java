package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.Character;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CharacterRepository extends JpaRepository<Character, UUID> {
    Optional<Character> findByIdAndUserId(UUID id, UUID userId);
    Page<Character> findByUserId(UUID userId, Pageable pageable);
    Page<Character> findByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);
    List<Character> findTop10ByUserIdOrderByUpdatedAtDesc(UUID userId);
    @Query("select c from Character c where c.user.id = :uid and (lower(c.fullName) like lower(concat('%', :q, '%')) or lower(c.alias) like lower(concat('%', :q, '%')) or lower(c.nickname) like lower(concat('%', :q, '%')))")
    List<Character> searchByName(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    @Query("select c from Character c where c.user.id = :uid and (lower(c.fullName) like lower(concat('%', :q, '%')) or lower(c.backstory) like lower(concat('%', :q, '%')))")
    Page<Character> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    long countByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
