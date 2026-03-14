-- Separation/Exit Management
CREATE TABLE separations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    separation_type VARCHAR(30) NOT NULL,
    resignation_date DATE,
    last_working_date DATE,
    notice_period_days INT DEFAULT 30,
    reason TEXT,
    exit_interview_notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    final_settlement_amount DECIMAL(12,2),
    remaining_leaves DECIMAL(5,1),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE exit_checklist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    separation_id UUID NOT NULL REFERENCES separations(id),
    item_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    completed_by UUID,
    completed_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
CREATE INDEX idx_separations_tenant ON separations(tenant_id);
CREATE INDEX idx_separations_employee ON separations(employee_id);
CREATE INDEX idx_exit_checklist_separation ON exit_checklist_items(separation_id);
