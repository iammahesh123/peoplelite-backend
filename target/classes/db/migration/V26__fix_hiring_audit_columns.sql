-- V26: Fix audit column names in hiring tables to match AuditableEntity mapping
-- AuditableEntity expects: created_by, updated_by
-- V24 created them as: created_by_user, updated_by_user

-- candidates
ALTER TABLE candidates ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE candidates ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE candidates DROP COLUMN IF EXISTS created_by_user;
ALTER TABLE candidates DROP COLUMN IF EXISTS updated_by_user;

-- job_openings
ALTER TABLE job_openings ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE job_openings DROP COLUMN IF EXISTS created_by_user;
ALTER TABLE job_openings DROP COLUMN IF EXISTS updated_by_user;

-- interviews
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE interviews DROP COLUMN IF EXISTS created_by_user;
ALTER TABLE interviews DROP COLUMN IF EXISTS updated_by_user;

-- interview_feedback
ALTER TABLE interview_feedback ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE interview_feedback ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE interview_feedback DROP COLUMN IF EXISTS created_by_user;
ALTER TABLE interview_feedback DROP COLUMN IF EXISTS updated_by_user;

-- offers
ALTER TABLE offers ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE offers ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE offers DROP COLUMN IF EXISTS created_by_user;
ALTER TABLE offers DROP COLUMN IF EXISTS updated_by_user;
