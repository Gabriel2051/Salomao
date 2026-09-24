package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.CatalogItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CatalogItemRepository extends JpaRepository<CatalogItem, UUID> {
    Optional<CatalogItem> findByIdAndUserId(UUID id, UUID userId);
    Page<CatalogItem> findByUserId(UUID userId, Pageable pageable);
    Page<CatalogItem> findByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);
    Page<CatalogItem> findByUserIdAndKindIgnoreCase(UUID userId, String kind, Pageable pageable);
    List<CatalogItem> findByUserIdOrderByNameAsc(UUID userId);
    List<CatalogItem> findByOwnerIdAndUserIdOrderByNameAsc(UUID ownerId, UUID userId);
    @Query("select distinct i.kind from CatalogItem i where i.user.id = :uid and i.kind <> '' order by i.kind")
    List<String> distinctKinds(@Param("uid") UUID userId);
    @Query("select i from CatalogItem i where i.user.id = :uid and (lower(i.name) like lower(concat('%', :q, '%')) or lower(i.summary) like lower(concat('%', :q, '%')) or lower(i.kind) like lower(concat('%', :q, '%')) or lower(i.lore) like lower(concat('%', :q, '%')))")
    Page<CatalogItem> search(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    @Query("select i from CatalogItem i where i.user.id = :uid and lower(i.name) like lower(concat('%', :q, '%'))")
    List<CatalogItem> searchByName(@Param("uid") UUID userId, @Param("q") String q, Pageable pageable);
    long countByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
