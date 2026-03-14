-- ========================================================
-- V24: Hiring Management Module
-- Tables: job_openings, candidates, interviews, interview_feedback, offers, candidate_activities
-- ========================================================

-- Job Openings
CREATE TABLE job_openings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    title           VARCHAR(255) NOT NULL,
    department      VARCHAR(100),
    location        VARCHAR(200),
    employment_type VARCHAR(30) NOT NULL DEFAULT 'FULL_TIME',
    experience_min  INTEGER DEFAULT 0,
    experience_max  INTEGER,
    description     TEXT,
    salary_min      DECIMAL(12,2),
    salary_max      DECIMAL(12,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_by      UUID,
    closed_at       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by_user UUID,
    updated_by_user UUID
);

CREATE INDEX idx_job_openings_tenant ON job_openings(tenant_id);
CREATE INDEX idx_job_openings_status ON job_openings(tenant_id, status);

-- Candidates
CREATE TABLE candidates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    job_opening_id  UUID NOT NULL REFERENCES job_openings(id),
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(30),
    resume_url      VARCHAR(500),
    linkedin_url    VARCHAR(500),
    portfolio_url   VARCHAR(500),
    source          VARCHAR(50) DEFAULT 'MANUAL',
    stage           VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    notes           TEXT,
    applied_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by_user UUID,
    updated_by_user UUID
);

CREATE INDEX idx_candidates_tenant ON candidates(tenant_id);
CREATE INDEX idx_candidates_job ON candidates(tenant_id, job_opening_id);
CREATE INDEX idx_candidates_stage ON candidates(tenant_id, stage);

-- Interviews
CREATE TABLE interviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    candidate_id    UUID NOT NULL REFERENCES candidates(id),
    job_opening_id  UUID NOT NULL REFERENCES job_openings(id),
    interviewer_id  UUID REFERENCES employees(id),
    interview_date  TIMESTAMP NOT NULL,
    duration_minutes INTEGER DEFAULT 60,
    interview_type  VARCHAR(30) NOT NULL DEFAULT 'IN_PERSON',
    location        VARCHAR(300),
    meeting_link    VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by_user UUID,
    updated_by_user UUID
);

CREATE INDEX idx_interviews_tenant ON interviews(tenant_id);
CREATE INDEX idx_interviews_candidate ON interviews(tenant_id, candidate_id);

-- Interview Feedback
CREATE TABLE interview_feedback (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    interview_id    UUID NOT NULL REFERENCES interviews(id),
    reviewer_id     UUID REFERENCES employees(id),
    rating          INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    strengths       TEXT,
    weaknesses      TEXT,
    comments        TEXT,
    recommendation  VARCHAR(30) NOT NULL DEFAULT 'NEUTRAL',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by_user UUID,
    updated_by_user UUID
);

CREATE INDEX idx_feedback_tenant ON interview_feedback(tenant_id);
CREATE INDEX idx_feedback_interview ON interview_feedback(interview_id);

-- Offers
CREATE TABLE offers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    candidate_id    UUID NOT NULL REFERENCES candidates(id),
    job_opening_id  UUID NOT NULL REFERENCES job_openings(id),
    offered_salary  DECIMAL(12,2) NOT NULL,
    joining_date    DATE NOT NULL,
    designation     VARCHAR(200),
    department      VARCHAR(100),
    offer_notes     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMP,
    responded_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by_user UUID,
    updated_by_user UUID
);

CREATE INDEX idx_offers_tenant ON offers(tenant_id);
CREATE INDEX idx_offers_candidate ON offers(tenant_id, candidate_id);

-- Candidate Activity Timeline
CREATE TABLE candidate_activities (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    candidate_id    UUID NOT NULL REFERENCES candidates(id),
    activity_type   VARCHAR(50) NOT NULL,
    description     TEXT NOT NULL,
    performed_by    UUID,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_activities_tenant ON candidate_activities(tenant_id);
CREATE INDEX idx_activities_candidate ON candidate_activities(tenant_id, candidate_id);
