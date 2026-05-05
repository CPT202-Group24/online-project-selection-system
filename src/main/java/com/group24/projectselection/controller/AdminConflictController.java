package com.group24.projectselection.controller;

import com.group24.projectselection.model.ConflictLog;
import com.group24.projectselection.repository.ConflictLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class AdminConflictController {

    private final ConflictLogRepository conflictLogRepository;

    public AdminConflictController(ConflictLogRepository conflictLogRepository) {
        this.conflictLogRepository = conflictLogRepository;
    }

    @GetMapping("/admin/conflicts")
    public String showConflictLogs(Model model) {
        List<ConflictLog> conflictLogs = new ArrayList<>(conflictLogRepository.findAll());
        Collections.reverse(conflictLogs);
        model.addAttribute("conflictLogs", conflictLogs);
        return "admin-conflicts";
    }
}
