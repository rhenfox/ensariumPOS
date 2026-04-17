package com.aldrin.ensarium.model;

import java.sql.Date;
import java.sql.Timestamp;

public record StoreFiscalProfileRow(
        int storeId,
        String storeCode,
        String storeName,
        int taxpayerProfileId,
        String taxpayerRegisteredName,
        String branchCode,
        String registeredBusinessAddress,
        String posVendorName,
        String posVendorTinNo,
        String posVendorAddress,
        String supplierAccreditationNo,
        Date accreditationIssuedAt,
        Date accreditationValidUntil,
        String birPermitToUseNo,
        Date permitToUseIssuedAt,
        String atpNo,
        Date atpIssuedAt,
        boolean active,
        Timestamp updatedAt
) {}
