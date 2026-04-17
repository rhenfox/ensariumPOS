package com.aldrin.ensarium.model;

import java.sql.Timestamp;

public record ReceiptSeriesRow(
        long id,
        int terminalId,
        String terminalCode,
        String storeCode,
        String storeName,
        String docType,
        String prefix,
        long serialFrom,
        long serialTo,
        long nextSerial,
        boolean active,
        Timestamp createdAt
) {}
