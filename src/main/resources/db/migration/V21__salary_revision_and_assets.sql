-- Salary Revision Tracking
CREATE TABLE salary_revisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    previous_ctc DECIMAL(12,2),
    new_ctc DECIMAL(12,2) NOT NULL,
    previous_basic DECIMAL(12,2),
    new_basic DECIMAL(12,2),
    effective_date DATE NOT NULL,
    reason VARCHAR(500),
    remarks TEXT,
    revised_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_salary_revisions_employee ON salary_revisions(employee_id);
CREATE INDEX idx_salary_revisions_tenant ON salary_revisions(tenant_id);

-- Company Assets Tracking
CREATE TABLE company_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    asset_name VARCHAR(200) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100),
    assigned_to UUID REFERENCES employees(id),
    assigned_date DATE,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_assets_tenant ON company_assets(tenant_id);
CREATE INDEX idx_assets_assigned ON company_assets(assigned_to);
