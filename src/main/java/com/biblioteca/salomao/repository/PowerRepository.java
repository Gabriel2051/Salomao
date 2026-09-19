package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.Character;
import com.biblioteca.salomao.domain.Power;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface PowerRepository extends JpaRepository<Power, UUID> {
    Optional<Power> findByIdAndUserId(UUID id, UUID userId);
    Page<Power> findByUserId(UUID userId, Pageable pageable);
    List<Power> findByUserIdOrderByNameAsc(UUID userId);
    @Query("select p from Power p where p.user.id = :uid and (lower(p.name) like lower(concat('%', :q, '%')) or lower(p.description) like lower(concat('%', :q, '%')))")
    Page<Power> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    @Query("select p from Power p where p.user.id = :uid and lower(p.name) like lower(concat('%', :q, '%'))")
    List<Power> searchByName(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    long countByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    @Query("select c from Character c join c.powers p where p.id = :pid and c.user.id = :uid")
    List<Character> findCharactersOfPower(@Param("pid") UUID powerId, @Param("uid") UUID userId);
}
