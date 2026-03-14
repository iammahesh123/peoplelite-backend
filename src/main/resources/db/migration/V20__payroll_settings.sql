-- V20: Payroll compliance settings per tenant
-- Small companies may not have PF/ESI/PT registration

CREATE TABLE payroll_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) UNIQUE,

    -- Provident Fund
    pf_enabled BOOLEAN NOT NULL DEFAULT false,
    pf_rate DECIMAL(5,2) NOT NULL DEFAULT 12.00,
    pf_cap DECIMAL(12,2) NOT NULL DEFAULT 1800.00,

    -- ESI (Employee State Insurance)
    esi_enabled BOOLEAN NOT NULL DEFAULT false,
    esi_employee_rate DECIMAL(5,2) NOT NULL DEFAULT 0.75,
    esi_employer_rate DECIMAL(5,2) NOT NULL DEFAULT 3.25,
    esi_wage_ceiling DECIMAL(12,2) NOT NULL DEFAULT 21000.00,

    -- Professional Tax
    pt_enabled BOOLEAN NOT NULL DEFAULT false,
    pt_amount DECIMAL(12,2) NOT NULL DEFAULT 200.00,

    -- TDS (Tax Deducted at Source)
    tds_enabled BOOLEAN NOT NULL DEFAULT false,

    -- Employer PF contribution (shown separately, not deducted from employee)
    pf_employer_enabled BOOLEAN NOT NULL DEFAULT false,
    pf_employer_rate DECIMAL(5,2) NOT NULL DEFAULT 12.00,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE UNIQUE INDEX idx_payroll_settings_tenant ON payroll_settings(tenant_id);

-- Add ESI columns to payslips for when ESI is enabled
ALTER TABLE payslips ADD COLUMN esi_employee DECIMAL(12,2) DEFAULT 0;
ALTER TABLE payslips ADD COLUMN esi_employer DECIMAL(12,2) DEFAULT 0;
ALTER TABLE payslips ADD COLUMN pf_employer DECIMAL(12,2) DEFAULT 0;
ALTER TABLE payslips ADD COLUMN tds DECIMAL(12,2) DEFAULT 0;
