package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/applications")
public class TeacherApprovalController {

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    // ==========================================
    // Sprint 3: PBI 6.3 - 查看已录用学生名单
    // ==========================================
    @GetMapping("/topics/{topicId}/students")
    public ResponseEntity<?> viewAcceptedStudents(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId) {
        try {
            List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);
            return ResponseEntity.ok(acceptedApps);
        } catch (ResponseStatusException e) {
            // 明确捕获权限异常，强制返回 403 Forbidden，防止被 main 分支全局异常拦截器误杀为 500
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: " + e.getReason());
        } catch (Exception e) {
            // 兜底其他未知错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Sprint 3: PBI 6.3 - 导出已录用学生名单 (CSV格式)
    // ==========================================
    @GetMapping("/topics/{topicId}/students/export")
    public void exportAcceptedStudentsCsv(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId,
            HttpServletResponse response) throws IOException {
        try {
            List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);

            // 设置响应头，告诉浏览器这是一个 CSV 文件下载
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"accepted_students_topic_" + topicId + ".csv\"");

            PrintWriter writer = response.getWriter();
            // 写入 BOM 头，防止用 Excel 打开 CSV 时中文乱码 (严谨的细节)
            writer.write('\ufeff');

            // 注意：这里已经按照之前的逻辑，使用 Email 代替了不存在的学号
            writer.println("Student Name,Student Email");

            for (Application app : acceptedApps) {
                String studentName = app.getStudent().getName();
                String studentEmail = app.getStudent().getEmail();
                writer.println(studentName + "," + studentEmail);
            }
            writer.flush();

        } catch (ResponseStatusException e) {
            // 导出文件时如果越权，同样强制写回 403 状态
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: " + e.getReason());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}
