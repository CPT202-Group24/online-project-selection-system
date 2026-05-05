package com.group24.projectselection.service;

import com.group24.projectselection.model.AuditLog;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Sprint 3 — M8 PBI 8.3（审计日志）Service 层单元测试（指南方式 A：Mockito mock 仓储）。
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void log_正常情况_合法管理员应写入一条审计记录() {
        User admin = new User();
        admin.setId(42L);
        admin.setEmail("admin@test.com");

        auditLogService.log(admin, AuditLogService.ACTION_USER_STATUS_TOGGLE, AuditLogService.ENTITY_USER, 100L);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertEquals(admin, saved.getAdmin());
        assertEquals(AuditLogService.ACTION_USER_STATUS_TOGGLE, saved.getActionType());
        assertEquals(AuditLogService.ENTITY_USER, saved.getEntityType());
        assertEquals(100L, saved.getEntityId());
    }

    @Test
    void log_异常情况_admin为null时不应调用save() {
        auditLogService.log(null, AuditLogService.ACTION_CATEGORY_DELETE, AuditLogService.ENTITY_CATEGORY, 1L);

        verifyNoInteractions(auditLogRepository);
    }
}
