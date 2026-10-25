CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_hours INT NOT NULL DEFAULT 40,
    instructor VARCHAR(150),
    course_type VARCHAR(50) NOT NULL DEFAULT 'ONLINE',
    rating DOUBLE PRECISION DEFAULT 4.5,
    total_enrolled INT DEFAULT 0,
    total_completed INT DEFAULT 0,
    category VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS learning_paths (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_role VARCHAR(150),
    total_courses INT DEFAULT 1,
    progress_percentage INT DEFAULT 0,
    courses_summary VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS course_enrollments (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    employee_id BIGINT,
    student_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'ENROLLED',
    progress_percentage INT NOT NULL DEFAULT 0,
    enrolled_at DATE NOT NULL,
    completed_at DATE
);

CREATE TABLE IF NOT EXISTS career_plans (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT,
    student_id BIGINT,
    current_role_title VARCHAR(150) NOT NULL,
    target_role VARCHAR(150) NOT NULL,
    progress_percentage INT DEFAULT 0,
    skill_gaps VARCHAR(500),
    mentor_name VARCHAR(150),
    promotion_readiness_score INT DEFAULT 0,
    promotion_eligibility VARCHAR(150),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS internal_jobs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    department VARCHAR(150) NOT NULL,
    description TEXT,
    required_skills VARCHAR(500) NOT NULL,
    openings_count INT DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE IF NOT EXISTS job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES internal_jobs(id) ON DELETE CASCADE,
    employee_id BIGINT,
    student_id BIGINT,
    applied_at DATE NOT NULL,
    match_percentage INT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED'
);
