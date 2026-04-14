package com.group24.projectselection.service;

import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectTopicRepository projectTopicRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ProjectTopicRepository projectTopicRepository) {
        this.applicationRepository = applicationRepository;
        this.projectTopicRepository = projectTopicRepository;
    }
}
