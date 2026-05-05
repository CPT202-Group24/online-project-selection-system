package com.group24.projectselection.service;

import com.group24.projectselection.model.AuditLog;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    public static final String ACTION_USER_STATUS_TOGGLE = "USER_STATUS_TOGGLE";
    public static final String ACTION_CATEGORY_DELETE = "CATEGORY_DELETE";

    public static final String ENTITY_USER = "User";
    public static final String ENTITY_CATEGORY = "Category";

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User admin, String actionType, String entityType, Long entityId) {
        if (admin == null || admin.getId() == null) {
            return;
        }
        AuditLog entry = new AuditLog();
        entry.setAdmin(admin);
        entry.setActionType(actionType);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        auditLogRepository.save(entry);
    }

    public Map<String, Object> findPage(String query, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> result;
        if (!StringUtils.hasText(query)) {
            result = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            result = auditLogRepository.searchByKeyword(query.trim(), pageable);
        }

        List<AuditLogEntry> items = result.getContent().stream().map(this::toEntry).toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalItems", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        return body;
    }

    private AuditLogEntry toEntry(AuditLog a) {
        String adminEmail = a.getAdmin() != null ? a.getAdmin().getEmail() : "";
        return new AuditLogEntry(
                a.getId(),
                adminEmail,
                a.getActionType(),
                a.getEntityType(),
                a.getEntityId(),
                a.getCreatedAt()
        );
    }

    public record AuditLogEntry(
            Long id,
            String adminEmail,
            String actionType,
            String entityType,
            Long entityId,
            java.time.LocalDateTime createdAt
    ) {
    }
}
