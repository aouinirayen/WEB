-- SQL Migration Script for LegalDocument Simplification (Phase 2)
-- Removes soft delete and other unused fields from legal_documents table
-- Compatible with MySQL 8.0

-- Remove unused columns from legal_documents table
ALTER TABLE legal_documents DROP COLUMN IF EXISTS rejection_reason;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS custom_fields;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS is_deleted;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS expiry_notification_sent_at;

-- Verify the changes
DESCRIBE legal_documents;

