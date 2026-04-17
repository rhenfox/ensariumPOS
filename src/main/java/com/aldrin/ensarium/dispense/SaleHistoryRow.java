package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SaleHistoryRow {
    public long saleId;
    public String saleNo;
    public String customerName;
    public Timestamp soldAt;
    public BigDecimal total = BigDecimal.ZERO;
    public String invoiceNo;
    public int returnCount;
    public boolean canEdit;
    public String editStatus;

    @Override
    public String toString() {
        return saleNo;
    }
}
