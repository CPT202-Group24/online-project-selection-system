package com.group24.projectselection;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptSeedPasswordTest {

    private static final String SEEDED_HASH =
            "$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO";

    @Test
    void seededPasswordTest123ShouldMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(encoder.matches("test123", SEEDED_HASH));
        assertFalse(encoder.matches("wrong123", SEEDED_HASH));
    }
}