package com.group24.projectselection.controller;

import com.group24.projectselection.service.AdminViolationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViolationController {

    private final AdminViolationService adminViolationService;

    public AdminViolationController(AdminViolationService adminViolationService) {
        this.adminViolationService = adminViolationService;
    }

    @GetMapping("/admin/violations")
    public String violationPage(Model model) {
        model.addAttribute("violations", adminViolationService.getViolations());
        return "admin-violations";
    }
}
