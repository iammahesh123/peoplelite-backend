CREATE TABLE IF NOT EXISTS document_expiry_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    employee_id UUID NOT NULL,
    employee_name VARCHAR(255),
    document_type VARCHAR(100) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    issue_date DATE,
    expiry_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    reminder_sent_30 BOOLEAN NOT NULL DEFAULT false,
    reminder_sent_15 BOOLEAN NOT NULL DEFAULT false,
    reminder_sent_7 BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);
CREATE INDEX idx_doc_expiry_tenant ON document_expiry_records(tenant_id);
CREATE INDEX idx_doc_expiry_date ON document_expiry_records(expiry_date);
CREATE INDEX idx_doc_expiry_employee ON document_expiry_records(employee_id);
