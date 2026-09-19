package com.biblioteca.salomao.repository;
import com.biblioteca.salomao.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findTop15ByUserIdOrderByCreatedAtDesc(UUID userId);
}
