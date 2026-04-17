package com.aldrin.ensarium.dispense;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HeldSale implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;
    public LocalDateTime heldAt;
    public Long customerId;
    public String customerName;
    public SaleDiscountInfo saleDiscount;
    public List<CartLine> lines = new ArrayList<>();
}
