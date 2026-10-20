package com.skillsphere.skill.entity;

import java.time.LocalDate;

public enum CertificationStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    PENDING_VERIFICATION;

    /** Missing expiry dates mean ACTIVE; authenticity is represented by VerificationStatus separately. */
    public static CertificationStatus fromDates(LocalDate expiryDate) {
        if (expiryDate == null) {
            return ACTIVE;
        }

        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(30))) {
            return EXPIRING_SOON;
        }
        return ACTIVE;
    }
}
