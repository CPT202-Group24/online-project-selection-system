package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import java.util.List;

public interface TeacherApprovalService {

    void processApproval(Long applicationId, boolean isAccepted);

    void processApproval(Long applicationId, boolean isAccepted, Long currentTeacherId);

    List<Application> getAcceptedApplications(Long topicId, Long currentTeacherId);

    // 🌟 缺的就是下面这一行，必须把它加进来！
    List<Application> getPendingApplicationsByEmail(String email);
}
