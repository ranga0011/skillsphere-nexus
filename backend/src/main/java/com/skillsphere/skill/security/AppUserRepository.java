package com.skillsphere.skill.security;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByRole(UserRole role);

    boolean existsByEmployeeId(Long employeeId);

    boolean existsByStudentId(Long studentId);

    long countByRoleAndEnabledTrue(UserRole role);

    Page<AppUser> findAllByOrderById(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from AppUser user where user.role = :role and user.enabled = true")
    java.util.List<AppUser> findEnabledByRoleForUpdate(@Param("role") UserRole role);
}