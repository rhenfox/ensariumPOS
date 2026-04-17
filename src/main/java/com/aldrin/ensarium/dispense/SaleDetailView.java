package com.aldrin.ensarium.dispense;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SaleDetailView {
    public long saleId;
    public String saleNo;
    public String invoiceNo;
    public Timestamp soldAt;
    public String customerName;
    public String cashierUsername;
    public String notes;
    public int itemCount;
    public BigDecimal total = BigDecimal.ZERO;
    public final List<SaleDetailLine> lines = new ArrayList<>();
}
