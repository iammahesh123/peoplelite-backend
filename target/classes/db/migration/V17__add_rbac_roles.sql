-- Add new roles to the users table
-- PostgreSQL enum extension: no action needed since role is VARCHAR/enum string in JPA

-- Add a permissions table for fine-grained access
CREATE TABLE IF NOT EXISTS role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(100) NOT NULL,
    granted BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(tenant_id, role, permission)
);

-- Default permissions seed
INSERT INTO role_permissions (tenant_id, role, permission, granted)
SELECT t.id, r.role, p.permission, true
FROM tenants t
CROSS JOIN (VALUES ('HR_MANAGER'), ('DEPARTMENT_HEAD'), ('ACCOUNTANT')) AS r(role)
CROSS JOIN (VALUES
    ('employees.view'), ('employees.create'), ('employees.edit'),
    ('leaves.view'), ('leaves.approve'),
    ('payroll.view'), ('payroll.generate'),
    ('documents.view'), ('documents.create'),
    ('onboarding.view'), ('onboarding.manage'),
    ('audit.view'), ('settings.view'), ('settings.edit'),
    ('announcements.view'), ('announcements.create')
) AS p(permission)
WHERE (r.role = 'HR_MANAGER')
   OR (r.role = 'DEPARTMENT_HEAD' AND p.permission IN ('employees.view', 'leaves.view', 'leaves.approve', 'documents.view', 'onboarding.view', 'announcements.view'))
   OR (r.role = 'ACCOUNTANT' AND p.permission IN ('employees.view', 'payroll.view', 'payroll.generate', 'documents.view'))
ON CONFLICT DO NOTHING;
