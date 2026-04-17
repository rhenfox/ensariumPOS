package com.aldrin.ensarium.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record ShiftRow(
        long id,
        int storeId,
        String storeName,
        int terminalId,
        String terminalCode,
        Integer openedBy,
        String openedByName,
        Timestamp openedAt,
        Integer closedBy,
        String closedByName,
        Timestamp closedAt,
        BigDecimal openingCash,
        BigDecimal closingCash,
        String status,
        String remarks
) {}
