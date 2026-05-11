-- Migration: Add user ban/deactivation feature
-- This script adds the inactivatedUntil column to the users table
-- to support temporary and permanent user bans

ALTER TABLE users ADD COLUMN inactivated_until DATETIME NULL;

-- Add an index on inactivated_until for faster queries
CREATE INDEX idx_inactivated_until ON users(inactivated_until);

