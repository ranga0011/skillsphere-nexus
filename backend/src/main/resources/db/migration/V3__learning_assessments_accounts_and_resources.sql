ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS account_status_audit (
    id BIGSERIAL PRIMARY KEY,
    target_user_id BIGINT NOT NULL REFERENCES app_users(id),
    actor_username VARCHAR(150) NOT NULL,
    enabled_status BOOLEAN NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_account_status_audit_changed_at
    ON account_status_audit (changed_at DESC);

CREATE TABLE IF NOT EXISTS assessment_definitions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    instructions VARCHAR(1000) NOT NULL,
    skill_id BIGINT NOT NULL,
    pass_percentage INTEGER NOT NULL CHECK (pass_percentage BETWEEN 1 AND 100),
    max_attempts INTEGER NOT NULL DEFAULT 3 CHECK (max_attempts BETWEEN 1 AND 10),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_assessment_definitions_skill_active
    ON assessment_definitions (skill_id, active);

CREATE TABLE IF NOT EXISTS assessment_questions (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessment_definitions(id) ON DELETE CASCADE,
    prompt VARCHAR(1000) NOT NULL,
    correct_option_index INTEGER NOT NULL CHECK (correct_option_index >= 0),
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    points INTEGER NOT NULL DEFAULT 1 CHECK (points BETWEEN 1 AND 10)
);

CREATE INDEX IF NOT EXISTS idx_assessment_questions_assessment
    ON assessment_questions (assessment_id);

CREATE TABLE IF NOT EXISTS assessment_question_options (
    question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
    option_order INTEGER NOT NULL CHECK (option_order >= 0),
    option_text VARCHAR(500) NOT NULL,
    PRIMARY KEY (question_id, option_order)
);

CREATE TABLE IF NOT EXISTS learner_assessment_attempts (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessment_definitions(id) ON DELETE RESTRICT,
    employee_id BIGINT,
    student_id BIGINT,
    skill_assessment_id BIGINT,
    attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('IN_PROGRESS', 'SUBMITTED')),
    score_points INTEGER CHECK (score_points >= 0),
    max_points INTEGER CHECK (max_points > 0),
    percentage INTEGER CHECK (percentage BETWEEN 0 AND 100),
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_learner_attempt_owner CHECK (
        (employee_id IS NOT NULL AND student_id IS NULL)
        OR (employee_id IS NULL AND student_id IS NOT NULL)
    ),
    CONSTRAINT uq_employee_attempt_number UNIQUE (assessment_id, employee_id, attempt_number),
    CONSTRAINT uq_student_attempt_number UNIQUE (assessment_id, student_id, attempt_number)
);

CREATE INDEX IF NOT EXISTS idx_learner_attempt_employee_started
    ON learner_assessment_attempts (employee_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_learner_attempt_student_started
    ON learner_assessment_attempts (student_id, started_at DESC);

CREATE TABLE IF NOT EXISTS learner_attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES learner_assessment_attempts(id) ON DELETE RESTRICT,
    question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE RESTRICT,
    selected_option_index INTEGER NOT NULL CHECK (selected_option_index >= 0),
    CONSTRAINT uq_learner_attempt_question UNIQUE (attempt_id, question_id)
);

CREATE TABLE IF NOT EXISTS learning_resources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    skill_id BIGINT NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    verified_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_learning_resources_skill_active
    ON learning_resources (skill_id, active);
