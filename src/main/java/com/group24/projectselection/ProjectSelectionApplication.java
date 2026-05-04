package com.group24.projectselection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjectSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSelectionApplication.class, args);
    }
}
