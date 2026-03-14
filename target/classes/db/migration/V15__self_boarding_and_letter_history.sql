-- Self-boarding invitation tracking
CREATE TABLE IF NOT EXISTS self_boarding_invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sbi_token ON self_boarding_invitations(token);
CREATE INDEX idx_sbi_tenant ON self_boarding_invitations(tenant_id);
CREATE INDEX idx_sbi_employee ON self_boarding_invitations(employee_id);

-- Letter generation history / audit
CREATE TABLE IF NOT EXISTS letter_generation_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID REFERENCES employees(id),
    employee_name VARCHAR(255),
    letter_type VARCHAR(50) NOT NULL,
    template_style VARCHAR(50),
    generated_by UUID,
    generated_at TIMESTAMP DEFAULT NOW(),
    emailed_to VARCHAR(255),
    emailed_at TIMESTAMP
);

CREATE INDEX idx_lgl_tenant ON letter_generation_log(tenant_id);
CREATE INDEX idx_lgl_employee ON letter_generation_log(employee_id);
