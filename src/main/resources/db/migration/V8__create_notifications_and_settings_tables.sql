-- V8: Notifications, settings, and Google auth support

-- Add google_id and auth_provider columns to users
ALTER TABLE users ADD COLUMN google_id VARCHAR(255);
ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
-- Make password nullable for Google-only users
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

CREATE INDEX idx_users_google_id ON users(google_id);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, read, created_at DESC);
CREATE INDEX idx_notifications_tenant ON notifications(tenant_id);

-- Tenant notification settings
CREATE TABLE tenant_notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id),
    email_leave_requests BOOLEAN DEFAULT TRUE,
    email_leave_approvals BOOLEAN DEFAULT TRUE,
    email_payroll_ready BOOLEAN DEFAULT TRUE,
    email_new_employee BOOLEAN DEFAULT TRUE,
    email_document_uploaded BOOLEAN DEFAULT TRUE,
    email_system_alerts BOOLEAN DEFAULT TRUE,
    inapp_leave_requests BOOLEAN DEFAULT TRUE,
    inapp_payroll BOOLEAN DEFAULT TRUE,
    inapp_onboarding BOOLEAN DEFAULT TRUE,
    inapp_documents BOOLEAN DEFAULT TRUE,
    inapp_system BOOLEAN DEFAULT TRUE,
    quiet_hours_enabled BOOLEAN DEFAULT FALSE,
    quiet_hours_start TIME DEFAULT '22:00',
    quiet_hours_end TIME DEFAULT '08:00',
    digest_frequency VARCHAR(20) DEFAULT 'daily',
    updated_at TIMESTAMP
);

-- Tenant email settings
CREATE TABLE tenant_email_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id),
    smtp_host VARCHAR(255),
    smtp_port INT DEFAULT 587,
    smtp_username VARCHAR(255),
    smtp_password VARCHAR(255),
    from_email VARCHAR(255),
    from_name VARCHAR(255),
    use_tls BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP
);

-- Tenant payment settings
CREATE TABLE tenant_payment_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id),
    razorpay_key_id VARCHAR(255),
    razorpay_key_secret VARCHAR(255),
    razorpay_webhook_secret VARCHAR(255),
    billing_legal_name VARCHAR(255),
    billing_gstin VARCHAR(50),
    billing_address TEXT,
    billing_email VARCHAR(255),
    billing_currency VARCHAR(10) DEFAULT 'INR',
    updated_at TIMESTAMP
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);

-- Platform feature flags
CREATE TABLE platform_feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN DEFAULT FALSE,
    scope VARCHAR(20) DEFAULT 'GLOBAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Platform settings (key-value store)
CREATE TABLE platform_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    category VARCHAR(50),
    updated_at TIMESTAMP
);
