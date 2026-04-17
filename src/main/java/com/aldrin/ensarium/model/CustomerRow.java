package com.aldrin.ensarium.model;

import java.sql.Timestamp;

public record CustomerRow(
        long id,
        String customerNo,
        String fullName,
        String tinNo,
        String phone,
        String email,
        String address,
        boolean senior,
        String seniorIdNo,
        boolean vatExempt,
        boolean active,
        Timestamp createdAt
) {}
