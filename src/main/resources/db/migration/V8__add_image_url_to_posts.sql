-- Optional image attached to a community post (path only, binary lives in uploads/media).
ALTER TABLE posts ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000);
