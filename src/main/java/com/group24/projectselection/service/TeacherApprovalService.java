package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import java.util.List;

public interface TeacherApprovalService {

    void processApproval(Long applicationId, boolean isAccepted);

    void processApproval(Long applicationId, boolean isAccepted, Long currentTeacherId);

    List<Application> getAcceptedApplications(Long topicId, Long currentTeacherId);
}
