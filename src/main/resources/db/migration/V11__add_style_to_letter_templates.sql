-- V11: Add style field to letter and salary slip templates
-- Add style column to offer_letter_templates
ALTER TABLE offer_letter_templates ADD COLUMN IF NOT EXISTS style VARCHAR(50) NOT NULL DEFAULT 'CORPORATE';

-- Add style column to joining_letter_templates
ALTER TABLE joining_letter_templates ADD COLUMN IF NOT EXISTS style VARCHAR(50) NOT NULL DEFAULT 'CORPORATE';

-- Add style column to salary_slip_templates
ALTER TABLE salary_slip_templates ADD COLUMN IF NOT EXISTS style VARCHAR(50) NOT NULL DEFAULT 'CORPORATE';

-- Create index for style queries
CREATE INDEX IF NOT EXISTS idx_offer_letter_templates_style ON offer_letter_templates(style);
CREATE INDEX IF NOT EXISTS idx_joining_letter_templates_style ON joining_letter_templates(style);
CREATE INDEX IF NOT EXISTS idx_salary_slip_templates_style ON salary_slip_templates(style);