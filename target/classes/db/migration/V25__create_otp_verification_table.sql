CREATE TABLE IF NOT EXISTS email_verification_otps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    company_name VARCHAR(255),
    full_name VARCHAR(255),
    password_hash VARCHAR(255),
    verified BOOLEAN DEFAULT false,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_otp_email ON email_verification_otps(email);
CREATE INDEX IF NOT EXISTS idx_otp_code ON email_verification_otps(otp_code, email);
