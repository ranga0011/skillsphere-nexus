package com.skillsphere.skill.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService {

    public boolean isHrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_HR_ADMIN"));
    }

    public boolean isStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
    }

    public boolean isCurrentEmployee(Long employeeId) {
        if (isHrAdmin()) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }
        Long currentEmployeeId = JwtService.employeeIdFromClaims(jwtAuthentication.getToken().getClaims());
        return employeeId != null && employeeId.equals(currentEmployeeId);
    }

    public void requireEmployeeAccess(Long employeeId) {
        if (!isCurrentEmployee(employeeId)) {
            throw new AccessDeniedException("Access to this employee record is forbidden");
        }
    }

    public boolean isCurrentStudent(Long studentId) {
        if (isHrAdmin()) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }
        Long currentStudentId = JwtService.studentIdFromClaims(jwtAuthentication.getToken().getClaims());
        return studentId != null && studentId.equals(currentStudentId);
    }

    public void requireStudentAccess(Long studentId) {
        if (!isCurrentStudent(studentId)) {
            throw new AccessDeniedException("Access to this student record is forbidden");
        }
    }

    public void requireProfileAccess(Long employeeId, Long studentId) {
        if (employeeId != null) {
            requireEmployeeAccess(employeeId);
        } else if (studentId != null) {
            requireStudentAccess(studentId);
        } else {
            throw new AccessDeniedException("A linked profile is required");
        }
    }
}