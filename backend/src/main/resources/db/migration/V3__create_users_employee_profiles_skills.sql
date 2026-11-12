CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'EMPLOYEE',
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'PROJECT_MANAGER', 'RESOURCE_MANAGER', 'HR', 'EMPLOYEE'))
);

CREATE TABLE employee_profiles (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL UNIQUE,
    job_title           VARCHAR(100) NOT NULL,
    department          VARCHAR(100),
    seniority_level     VARCHAR(30)  NOT NULL,
    capacity_percentage INTEGER      NOT NULL DEFAULT 100,
    bio                 TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_employee_capacity CHECK (capacity_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_employee_seniority CHECK (seniority_level IN ('JUNIOR', 'MID', 'SENIOR', 'LEAD'))
);

CREATE TABLE skills (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    category    VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_skills (
    id                  BIGSERIAL   PRIMARY KEY,
    employee_profile_id BIGINT      NOT NULL,
    skill_id            BIGINT      NOT NULL,
    proficiency_level   VARCHAR(30) NOT NULL,
    years_of_experience INTEGER     DEFAULT 0,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_skills_profile
        FOREIGN KEY (employee_profile_id) REFERENCES employee_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE,
    CONSTRAINT uq_employee_skill UNIQUE (employee_profile_id, skill_id),
    CONSTRAINT chk_employee_skill_proficiency CHECK (proficiency_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    CONSTRAINT chk_employee_skill_years CHECK (years_of_experience >= 0)
);

ALTER TABLE allocations
    ADD CONSTRAINT fk_allocations_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_employee_profiles_user_id ON employee_profiles (user_id);
CREATE INDEX idx_employee_skills_profile_id ON employee_skills (employee_profile_id);
CREATE INDEX idx_employee_skills_skill_id ON employee_skills (skill_id);
CREATE INDEX idx_skills_name ON skills (name);
