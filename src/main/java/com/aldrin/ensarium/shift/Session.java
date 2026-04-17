package com.aldrin.ensarium.shift;

public record Session(
        int userId,
        String username,
        String fullName,
        int storeId,
        String storeCode,
        String storeName,
        int terminalId,
        String terminalCode,
        String terminalName
) {
}
