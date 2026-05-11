-- SQL Migration Script for PIDEV User Management with BLOB Photo Storage
-- Compatible with MySQL 8.0 (XAMPP)
-- Run this script in phpMyAdmin or MySQL command line

-- Add photo column as LONGBLOB to users table
ALTER TABLE users ADD COLUMN photo LONGBLOB NULL;

-- Add photoContentType column to store MIME type
ALTER TABLE users ADD COLUMN photo_content_type VARCHAR(100) NULL;

-- Optional: Add index for better performance
ALTER TABLE users ADD INDEX idx_created_at (created_at);
ALTER TABLE users ADD INDEX idx_role (role);

-- Verify the changes
DESCRIBE users;

