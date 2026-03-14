-- V10: Create salary slip templates table
CREATE TABLE salary_slip_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    html_content TEXT NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_salary_slip_templates_tenant ON salary_slip_templates(tenant_id);
CREATE INDEX idx_salary_slip_templates_default ON salary_slip_templates(tenant_id, is_default);

-- Add template_id to payslips table
ALTER TABLE payslips ADD COLUMN IF NOT EXISTS template_id UUID REFERENCES salary_slip_templates(id);
CREATE INDEX idx_payslips_template ON payslips(template_id);

-- Add designation and department columns to payslips if they don't exist
ALTER TABLE payslips ADD COLUMN IF NOT EXISTS designation VARCHAR(255);
ALTER TABLE payslips ADD COLUMN IF NOT EXISTS department VARCHAR(255);
ALTER TABLE payslips ADD COLUMN IF NOT EXISTS company_name VARCHAR(255);
