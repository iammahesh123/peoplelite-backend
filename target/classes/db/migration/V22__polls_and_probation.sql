-- Polls and Feedback
CREATE TABLE polls (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id UUID NOT NULL REFERENCES tenants(id),

                       question VARCHAR(500) NOT NULL,
                       poll_type VARCHAR(20) NOT NULL DEFAULT 'EMOJI',
                       is_anonymous BOOLEAN NOT NULL DEFAULT true,
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       expires_at TIMESTAMP,

                       created_by UUID,
                       updated_by UUID,

                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP
);

CREATE TABLE poll_responses (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                tenant_id UUID NOT NULL REFERENCES tenants(id),
                                poll_id UUID NOT NULL REFERENCES polls(id),
                                employee_id UUID NOT NULL REFERENCES employees(id),

                                response_value VARCHAR(50) NOT NULL,

                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP,
                                created_by UUID,
                                updated_by UUID,

                                UNIQUE(poll_id, employee_id)
);
CREATE INDEX idx_polls_tenant ON polls(tenant_id);
CREATE INDEX idx_poll_responses_poll ON poll_responses(poll_id);

-- Probation Tracking Fields
ALTER TABLE employees ADD COLUMN IF NOT EXISTS probation_end_date DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS probation_status VARCHAR(20) DEFAULT 'NOT_APPLICABLE';
