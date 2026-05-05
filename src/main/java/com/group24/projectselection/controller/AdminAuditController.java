package com.group24.projectselection.controller;

import com.group24.projectselection.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class AdminAuditController {

    private final AuditLogService auditLogService;

    public AdminAuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/audit")
    public String auditLogPage() {
        return "admin-audit";
    }

    @GetMapping("/api/admin/audit-logs")
    @ResponseBody
    public Map<String, Object> listAuditLogs(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditLogService.findPage(q, page, size);
    }
}
