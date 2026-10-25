CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    program VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE app_users
    ADD COLUMN student_id BIGINT UNIQUE REFERENCES students(id);

ALTER TABLE app_users
    DROP CONSTRAINT IF EXISTS app_users_role_check;

ALTER TABLE app_users
    DROP CONSTRAINT IF EXISTS CONSTRAINT_4AA;

ALTER TABLE app_users
    ADD CONSTRAINT app_users_role_check CHECK (
        (role = 'HR_ADMIN' AND employee_id IS NULL AND student_id IS NULL)
        OR (role = 'EMPLOYEE' AND employee_id IS NOT NULL AND student_id IS NULL)
        OR (role = 'STUDENT' AND employee_id IS NULL AND student_id IS NOT NULL)
    );

ALTER TABLE IF EXISTS employee_skill_mappings
    ALTER COLUMN employee_id DROP NOT NULL;

ALTER TABLE IF EXISTS employee_skill_mappings
    ADD COLUMN IF NOT EXISTS student_id BIGINT REFERENCES students(id);

ALTER TABLE IF EXISTS employee_skill_mappings
    ADD CONSTRAINT ck_employee_skill_mapping_owner
        CHECK ((employee_id IS NOT NULL AND student_id IS NULL)
            OR (employee_id IS NULL AND student_id IS NOT NULL));

ALTER TABLE IF EXISTS skill_assessments
    ALTER COLUMN employee_id DROP NOT NULL;

ALTER TABLE IF EXISTS skill_assessments
    ADD COLUMN IF NOT EXISTS student_id BIGINT REFERENCES students(id);

ALTER TABLE IF EXISTS skill_assessments
    ADD CONSTRAINT ck_skill_assessment_owner
        CHECK ((employee_id IS NOT NULL AND student_id IS NULL)
            OR (employee_id IS NULL AND student_id IS NOT NULL));
