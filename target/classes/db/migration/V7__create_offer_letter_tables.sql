-- V7: Offer letter tables

CREATE TABLE offer_letter_templates (
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

CREATE INDEX idx_offer_templates_tenant ON offer_letter_templates(tenant_id);

CREATE TABLE offer_letters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    candidate_name VARCHAR(255) NOT NULL,
    candidate_email VARCHAR(255),
    template_id UUID NOT NULL REFERENCES offer_letter_templates(id),
    variables_json TEXT,
    pdf_storage_key VARCHAR(1000),
    generated_at TIMESTAMP,
    generated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_offer_letters_tenant ON offer_letters(tenant_id);
