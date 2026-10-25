package com.skillsphere.skill.security;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_status_audit")
public class AccountStatusAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "actor_username", nullable = false, length = 150)
    private String actorUsername;

    @Column(name = "enabled_status", nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant changedAt;

    protected AccountStatusAudit() {
    }

    public AccountStatusAudit(Long targetUserId, String actorUsername, boolean enabled) {
        this.targetUserId = targetUserId;
        this.actorUsername = actorUsername;
        this.enabled = enabled;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getTargetUserId() { return targetUserId; }
    public String getActorUsername() { return actorUsername; }
    public boolean isEnabled() { return enabled; }
    public Instant getChangedAt() { return changedAt; }
}
