package com.aldrin.ensarium.model;

import java.sql.Date;
import java.sql.Timestamp;

public record CustomerBenefitProfileRow(
        long id,
        long customerId,
        String customerName,
        String benefitType,
        String govIdNo,
        String tinNo,
        boolean active,
        Date effectiveFrom,
        Date effectiveTo,
        Timestamp createdAt
) {}
