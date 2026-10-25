package com.skillsphere.skill.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusAuditRepository extends JpaRepository<AccountStatusAudit, Long> {
}
