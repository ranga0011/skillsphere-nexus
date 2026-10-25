package com.skillsphere.skill.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillsphere.skill.security.AccountAdministrationService;
import com.skillsphere.skill.security.AccountStatusAudit;
import com.skillsphere.skill.service.LearningModels.AccountStatusChanged;
import com.skillsphere.skill.service.LearningModels.AccountStatusRequest;
import com.skillsphere.skill.service.LearningModels.UserAccountView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('HR_ADMIN')")
public class AccountAdministrationController {
    private final AccountAdministrationService accountService;

    public AccountAdministrationController(AccountAdministrationService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Page<UserAccountView> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return accountService.getUsers(page, size);
    }

    @PutMapping("/{id}/status")
    public AccountStatusChanged updateStatus(@PathVariable Long id,
            @Valid @RequestBody AccountStatusRequest request, Authentication authentication) {
        return accountService.updateStatus(id, request.enabled(), authentication.getName());
    }

    @GetMapping("/audit")
    public Page<AccountStatusAudit> getAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return accountService.getAudit(page, size);
    }
}
