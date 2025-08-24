-- Demo user data for development/testing purposes only
-- Passwords are BCrypt hashes: testuser/password123, admin/password123, officer/password123

INSERT INTO user_table (id, username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES 
(1, 'testuser', 'testuser@example.com', '$2a$10$dXJ3SW6G7P9wuQO4SwdOD.VWrMWVdQlJgTTKvNgfQhCynwg60kJNG', 'USER', true, true, true, true),
(2, 'admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P9wuQO4SwdOD.VWrMWVdQlJgTTKvNgfQhCynwg60kJNG', 'ADMIN', true, true, true, true),
(3, 'officer', 'officer@example.com', '$2a$10$dXJ3SW6G7P9wuQO4SwdOD.VWrMWVdQlJgTTKvNgfQhCynwg60kJNG', 'LOAN_OFFICER', true, true, true, true);