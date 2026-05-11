-- SQL Migration Script for LegalDocument Simplification
-- Removes unused fields from legal_documents table
-- Compatible with MySQL 8.0

-- Remove unused columns from legal_documents table
ALTER TABLE legal_documents DROP COLUMN IF EXISTS verified_by;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS verified_at;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS created_at;
ALTER TABLE legal_documents DROP COLUMN IF EXISTS updated_at;

-- Verify the changes
DESCRIBE legal_documents;

