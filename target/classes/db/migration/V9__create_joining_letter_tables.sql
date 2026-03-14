-- V9: Joining letter tables and link offer letters to employees

-- Add employee_id to offer_letters for linking to employee records
ALTER TABLE offer_letters ADD COLUMN employee_id UUID REFERENCES employees(id);
CREATE INDEX idx_offer_letters_employee ON offer_letters(employee_id);

-- Joining letter templates
CREATE TABLE joining_letter_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    html_content TEXT NOT NULL,
    variables_json TEXT,
    version INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_joining_templates_tenant ON joining_letter_templates(tenant_id);

-- Joining letters
CREATE TABLE joining_letters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    employee_name VARCHAR(255) NOT NULL,
    template_id UUID NOT NULL REFERENCES joining_letter_templates(id),
    variables_json TEXT,
    pdf_storage_key VARCHAR(1000),
    generated_at TIMESTAMP,
    generated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_joining_letters_tenant ON joining_letters(tenant_id);
CREATE INDEX idx_joining_letters_employee ON joining_letters(employee_id);
