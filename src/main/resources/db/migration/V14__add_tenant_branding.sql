-- Add company branding fields to tenants table
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS logo_url TEXT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ceo_signature_url TEXT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_address TEXT;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_phone VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_email VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_website VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_cin VARCHAR(50);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ceo_name VARCHAR(255);
