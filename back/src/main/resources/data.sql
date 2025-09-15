-- Optional: Create admin user for testing
INSERT INTO users (email, username, firstname, password, created_at, updated_at)
VALUES ('admin@admin.com', 'admin', 'Admin', '$2a$10$XURPShQ5u7x6eS4X4aTkGuO5u2an3W3.4./Y6N/4z0fLq3fM7bX.W', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

