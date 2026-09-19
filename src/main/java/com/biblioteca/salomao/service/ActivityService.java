package com.biblioteca.salomao.service;

import com.biblioteca.salomao.domain.ActivityLog;
import com.biblioteca.salomao.domain.User;
import com.biblioteca.salomao.repository.ActivityLogRepository;
import com.biblioteca.salomao.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityLogRepository log;
    private final UserRepository users;

    public ActivityService(ActivityLogRepository log, UserRepository users) {
        this.log = log;
        this.users = users;
    }

    @Transactional
    public void record(UUID userId, String action, String entityType, UUID entityId, String description) {
        User ref = users.getReferenceById(userId);
        ActivityLog a = new ActivityLog();
        a.setUser(ref);
        a.setAction(action);
        a.setEntityType(entityType == null ? "" : entityType);
        a.setEntityId(entityId);
        String d = description == null ? "" : description;
        a.setDescription(d.length() > 300 ? d.substring(0, 300) : d);
        log.save(a);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> recent(UUID userId) {
        return log.findTop15ByUserIdOrderByCreatedAtDesc(userId);
    }
}
