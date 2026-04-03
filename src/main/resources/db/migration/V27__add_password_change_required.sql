-- V27: Add password_change_required flag to users table
-- When founder creates an employee, the system generates a random password.
-- On first login, the employee must change this password.

ALTER TABLE users ADD COLUMN IF NOT EXISTS password_change_required BOOLEAN NOT NULL DEFAULT false;

-- Mark all existing employee users as NOT requiring password change
-- (they've presumably already logged in)
UPDATE users SET password_change_required = false WHERE password_change_required IS NULL;
