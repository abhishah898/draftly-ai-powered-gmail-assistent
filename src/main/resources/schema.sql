CREATE TABLE IF NOT EXISTS gmail_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    google_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(2000),
    access_token VARCHAR(2000),
    access_token_expiry_at TIMESTAMP NULL,
    UNIQUE (google_user_id),
    UNIQUE (email)
);