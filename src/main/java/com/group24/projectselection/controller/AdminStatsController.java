package com.group24.projectselection.controller;

import com.group24.projectselection.service.AdminStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/api/admin/stats")
    public Map<String, Long> stats() {
        return adminStatsService.getCounts();
    }
}
