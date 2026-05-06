package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminViolationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public AdminViolationService(ApplicationRepository applicationRepository,
                                 UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public List<ViolationRecord> getViolations() {
        List<Long> violatingStudentIds =
                applicationRepository.findStudentIdsWithMultipleAcceptedApplications();

        return violatingStudentIds.stream().map(studentId -> {
            User student = userRepository.findById(studentId).orElse(null);

            List<Application> violatingApps = applicationRepository
                    .findByStudentId(studentId)
                    .stream()
                    .filter(a -> a.getStatus() == Application.ApplicationStatus.accepted)
                    .collect(Collectors.toList());

            return new ViolationRecord(student, violatingApps);
        }).collect(Collectors.toList());
    }

    public record ViolationRecord(User student, List<Application> violatingApplications) {
    }
}
