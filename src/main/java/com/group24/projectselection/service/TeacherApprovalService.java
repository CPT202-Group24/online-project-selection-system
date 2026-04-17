package com.group24.projectselection.service;

public interface TeacherApprovalService {


    void processApproval(Long applicationId, boolean isAccepted);
}
