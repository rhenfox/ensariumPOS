package com.aldrin.ensarium.shift;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Shift(
        long id,
        int storeId,
        int terminalId,
        int openedBy,
        LocalDateTime openedAt,
        Integer closedBy,
        LocalDateTime closedAt,
        BigDecimal openingCash,
        BigDecimal closingCash,
        String status,
        String remarks
) {
}
