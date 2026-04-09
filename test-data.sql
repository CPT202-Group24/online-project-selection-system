-- CPT202 Group 24 - Test Data
-- Run this in MySQL Workbench after starting the application
-- Password for all accounts: test123

USE project_selection;

INSERT INTO users (email, password_hash, name, role, status, created_at) VALUES
('student@test.com',  '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Test Student', 'student', 'active', NOW()),
('teacher@test.com',  '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Test Teacher', 'teacher', 'active', NOW()),
('admin@test.com',    '$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO', 'Test Admin',   'admin',   'active', NOW());
