package com.group24.projectselection.service;

import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ProjectTopicRepository projectTopicRepository;
    private final ApplicationRepository applicationRepository;

    public AdminStatsService(UserRepository userRepository,
                             ProjectTopicRepository projectTopicRepository,
                             ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.projectTopicRepository = projectTopicRepository;
        this.applicationRepository = applicationRepository;
    }

    public Map<String, Long> getCounts() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("topicCount", projectTopicRepository.count());
        stats.put("applicationCount", applicationRepository.count());
        return stats;
    }
}
