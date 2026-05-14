-- ============================================================
-- VM Manager - Database Schema
-- PostgreSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL CHECK (role IN ('ADMIN', 'CLIENT')),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vms (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    cores       INTEGER       NOT NULL CHECK (cores > 0),
    ram         INTEGER       NOT NULL CHECK (ram > 0),
    disk        INTEGER       NOT NULL CHECK (disk > 0),
    os          VARCHAR(100)  NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'STOPPED'
                              CHECK (status IN ('RUNNING', 'STOPPED', 'PAUSED')),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email  ON users(email);
CREATE INDEX IF NOT EXISTS idx_vms_status   ON vms(status);
CREATE INDEX IF NOT EXISTS idx_vms_name     ON vms(name);
