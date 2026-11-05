-- Add avatar_url column to users table for profile photo support.
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);
