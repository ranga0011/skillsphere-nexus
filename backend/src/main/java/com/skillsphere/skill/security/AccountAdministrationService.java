package com.skillsphere.skill.security;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.skillsphere.skill.service.LearningModels.AccountStatusChanged;
import com.skillsphere.skill.service.LearningModels.UserAccountView;

@Service
@Transactional
public class AccountAdministrationService {
    private final AppUserRepository userRepository;
    private final AccountStatusAuditRepository auditRepository;

    public AccountAdministrationService(AppUserRepository userRepository,
            AccountStatusAuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserAccountView> getUsers(int page, int size) {
        validatePage(page, size);
        return userRepository.findAllByOrderById(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(AccountAdministrationService::toView);
    }

    public AccountStatusChanged updateStatus(Long id, boolean enabled, String actorUsername) {
        AppUser target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account was not found"));
        if (target.getUsername().equalsIgnoreCase(actorUsername) && !enabled) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot deactivate your own account");
        }
        if (target.isEnabled() == enabled) {
            return new AccountStatusChanged(toView(target), null);
        }
        if (!enabled && target.getRole() == UserRole.HR_ADMIN) {
            List<AppUser> activeAdmins = userRepository.findEnabledByRoleForUpdate(UserRole.HR_ADMIN);
            if (activeAdmins.size() <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The last active HR administrator cannot be deactivated");
            }
        }
        target.setEnabled(enabled);
        AppUser saved = userRepository.save(target);
        AccountStatusAudit audit = auditRepository.save(new AccountStatusAudit(saved.getId(), actorUsername, enabled));
        return new AccountStatusChanged(toView(saved), audit.getChangedAt());
    }

    @Transactional(readOnly = true)
    public Page<AccountStatusAudit> getAudit(int page, int size) {
        validatePage(page, size);
        return auditRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt")));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
    }

    private static UserAccountView toView(AppUser user) {
        return new UserAccountView(user.getId(), user.getUsername(), user.getRole().name(),
                user.getEmployeeId(), user.getStudentId(), user.isEnabled());
    }
}
