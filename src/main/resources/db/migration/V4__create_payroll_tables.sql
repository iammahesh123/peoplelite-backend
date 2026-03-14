-- V4: Payroll tables

CREATE TABLE payroll_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    month INT NOT NULL,
    year INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    run_date DATE,
    total_gross DECIMAL(14,2) DEFAULT 0,
    total_deductions DECIMAL(14,2) DEFAULT 0,
    total_net DECIMAL(14,2) DEFAULT 0,
    employee_count INT DEFAULT 0,
    generated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_payroll_runs_tenant ON payroll_runs(tenant_id);
CREATE UNIQUE INDEX idx_payroll_runs_unique ON payroll_runs(tenant_id, month, year);

CREATE TABLE payslips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    payroll_run_id UUID NOT NULL REFERENCES payroll_runs(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    employee_name VARCHAR(200),
    employee_code VARCHAR(20),
    gross_earnings DECIMAL(12,2),
    total_deductions DECIMAL(12,2),
    net_pay DECIMAL(12,2),
    basic DECIMAL(12,2),
    hra DECIMAL(12,2),
    special_allowance DECIMAL(12,2),
    pf_employee DECIMAL(12,2) DEFAULT 0,
    professional_tax DECIMAL(12,2) DEFAULT 0,
    other_deductions DECIMAL(12,2) DEFAULT 0,
    deduction_remarks TEXT,
    working_days INT,
    lop_days DOUBLE PRECISION DEFAULT 0,
    pdf_storage_key VARCHAR(500),
    month INT,
    year INT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_payslips_run ON payslips(payroll_run_id);
CREATE INDEX idx_payslips_employee ON payslips(employee_id);
