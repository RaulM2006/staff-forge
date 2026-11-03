CREATE TABLE projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    start_date  DATE         NOT NULL,
    end_date    DATE,
    status      VARCHAR(30)  NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_projects_dates  CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_projects_status CHECK (status IN ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE project_positions (
    id              BIGSERIAL    PRIMARY KEY,
    project_id      BIGINT       NOT NULL,
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    seniority_level VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_positions_project
        FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT chk_positions_seniority CHECK (seniority_level IN ('JUNIOR', 'MID', 'SENIOR', 'LEAD'))
);

CREATE TABLE allocations (
    id                    BIGSERIAL PRIMARY KEY,
    position_id           BIGINT    NOT NULL,
    user_id               BIGINT    NOT NULL,
    allocation_percentage INTEGER   NOT NULL,
    start_date            DATE      NOT NULL,
    end_date              DATE,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_allocations_position
        FOREIGN KEY (position_id) REFERENCES project_positions (id) ON DELETE RESTRICT,
    CONSTRAINT chk_allocations_percentage CHECK (allocation_percentage BETWEEN 1 AND 100),
    CONSTRAINT chk_allocations_dates      CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_positions_project_id   ON project_positions (project_id);
CREATE INDEX idx_allocations_position_id ON allocations (position_id);
CREATE INDEX idx_allocations_user_id     ON allocations (user_id);
