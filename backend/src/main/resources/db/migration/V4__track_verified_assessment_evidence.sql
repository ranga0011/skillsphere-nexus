ALTER TABLE IF EXISTS employee_skill_mappings
    ADD COLUMN IF NOT EXISTS verified_assessment_id BIGINT REFERENCES skill_assessments(id);
