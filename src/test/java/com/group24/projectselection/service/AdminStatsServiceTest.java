package com.group24.projectselection.service;

import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Sprint 3 — M8 PBI 8.4（系统统计）Service 层单元测试（指南方式 A：Mockito mock 仓储）。
 */
@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void getCounts_正常情况_三个仓储返回值应正确汇总() {
        when(userRepository.count()).thenReturn(12L);
        when(projectTopicRepository.count()).thenReturn(5L);
        when(applicationRepository.count()).thenReturn(8L);

        Map<String, Long> result = adminStatsService.getCounts();

        assertEquals(12L, result.get("userCount"));
        assertEquals(5L, result.get("topicCount"));
        assertEquals(8L, result.get("applicationCount"));
    }

    @Test
    void getCounts_异常情况_user仓储抛异常应向上抛出() {
        when(userRepository.count()).thenThrow(new RuntimeException("Simulated DB failure"));

        assertThrows(RuntimeException.class, () -> adminStatsService.getCounts());
    }
}
