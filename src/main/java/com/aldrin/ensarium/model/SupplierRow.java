package com.aldrin.ensarium.model;

import java.sql.Timestamp;

public record SupplierRow(
        long id,
        String supplierNo,
        String name,
        String phone,
        String email,
        String address,
        boolean active,
        Timestamp createdAt
        ) {

}
