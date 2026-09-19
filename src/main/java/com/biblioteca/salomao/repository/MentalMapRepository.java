package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.MentalMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface MentalMapRepository extends JpaRepository<MentalMap, UUID> {
    Optional<MentalMap> findByIdAndUserId(UUID id, UUID userId);
    Page<MentalMap> findByUserId(UUID userId, Pageable pageable);
    List<MentalMap> findTop10ByUserIdOrderByUpdatedAtDesc(UUID userId);
    @Query("select m from MentalMap m where m.user.id = :uid and lower(m.title) like lower(concat('%', :q, '%'))")
    Page<MentalMap> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    long countByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
