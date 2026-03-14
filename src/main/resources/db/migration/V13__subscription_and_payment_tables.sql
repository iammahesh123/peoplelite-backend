-- Plans table
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    max_employees INT NOT NULL DEFAULT 5,
    features TEXT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    plan_id UUID REFERENCES plans(id),
    status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    trial_starts_at TIMESTAMP,
    trial_ends_at TIMESTAMP,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    next_billing_date TIMESTAMP,
    razorpay_subscription_id VARCHAR(255),
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_trial_ends_at ON subscriptions(trial_ends_at);

-- Payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES subscriptions(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    razorpay_signature VARCHAR(500),
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    payment_method VARCHAR(100),
    error_message TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_subscription_id ON payments(subscription_id);
CREATE INDEX idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX idx_payments_razorpay_order_id ON payments(razorpay_order_id);
CREATE INDEX idx_payments_razorpay_payment_id ON payments(razorpay_payment_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Add trial_ends_at to tenants table
ALTER TABLE tenants ADD COLUMN trial_ends_at TIMESTAMP;

-- Seed default plans
INSERT INTO plans (name, code, description, price, currency, max_employees, features, display_order, is_active) VALUES
('Starter', 'STARTER', 'Perfect for small teams getting started', 999.00, 'INR', 25,
 'Up to 25 employees,Basic payroll processing,Leave management,Employee self-service portal,Email support',
 1, TRUE),
('Professional', 'PROFESSIONAL', 'Ideal for growing businesses', 2499.00, 'INR', 100,
 'Up to 100 employees,Advanced payroll with tax computation,Leave & attendance management,Document management,Offer & joining letters,Department management,Priority support',
 2, TRUE),
('Enterprise', 'ENTERPRISE', 'Complete solution for large organizations', 4999.00, 'INR', 10000,
 'Unlimited employees,Full payroll automation,Advanced compliance & reporting,Custom workflows,White-label solution,Dedicated account manager,24/7 phone support,API access',
 3, TRUE);
