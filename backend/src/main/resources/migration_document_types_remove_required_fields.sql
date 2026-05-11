-- SQL Migration Script for DocumentType - Remove requiredFields column
-- Compatible with MySQL 8.0

-- Remove requiredFields column from document_types table
ALTER TABLE document_types DROP COLUMN IF EXISTS required_fields;

-- Verify the changes
DESCRIBE document_types;

