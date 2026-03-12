INSERT INTO sys_role (role_name, role_code, remark)
VALUES
    ('System Administrator', 'ADMIN', 'System management and access control'),
    ('Risk User', 'RISK_USER', 'Risk data entry, assessment and warning handling'),
    ('Manager', 'MANAGER', 'Read-only analytics and warning visibility')
ON DUPLICATE KEY UPDATE remark = VALUES(remark);

-- Default admin user seed is intentionally not provided in phase 1.
-- A BCrypt password hash must be generated only after the password is confirmed.
