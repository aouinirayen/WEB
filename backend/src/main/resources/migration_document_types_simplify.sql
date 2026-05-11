-- SQL Migration Script for DocumentType Simplification
-- Removes unused fields from document_types table
-- Compatible with MySQL 8.0

-- Remove unused columns from document_types table
ALTER TABLE document_types DROP COLUMN IF EXISTS is_active;
ALTER TABLE document_types DROP COLUMN IF EXISTS created_by;
ALTER TABLE document_types DROP COLUMN IF EXISTS created_at;
ALTER TABLE document_types DROP COLUMN IF EXISTS updated_at;

-- Verify the changes
DESCRIBE document_types;

