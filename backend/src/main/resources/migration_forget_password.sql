-- Database Migration for Forget Password Feature
-- Add these columns to the users table to support password reset functionality

ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(255) DEFAULT NULL;
ALTER TABLE users ADD COLUMN password_reset_token_expiry DATETIME DEFAULT NULL;

-- Optional: Add index for faster token lookups
CREATE INDEX idx_password_reset_token ON users(password_reset_token);

