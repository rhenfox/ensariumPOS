package com.aldrin.ensarium.model;

import java.sql.Timestamp;

public record TaxpayerProfileRow(
        int id,
        String registeredName,
        String tradeName,
        String tinNo,
        String headOfficeAddress,
        String vatRegistrationType,
        boolean active,
        Timestamp createdAt
) {}
