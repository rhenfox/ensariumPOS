package com.aldrin.ensarium.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public record BenefitPolicyRow(
        int id,
        String code,
        String name,
        String benefitType,
        String kind,
        BigDecimal defaultRate,
        BigDecimal minRate,
        BigDecimal maxRate,
        boolean vatExempt,
        boolean allowManualOverride,
        String legalBasis,
        Date effectiveFrom,
        Date effectiveTo,
        boolean active,
        Timestamp createdAt
) {
}
