package com.aldrin.ensarium.order;


import com.aldrin.ensarium.inventory.product.ProductInventoryItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderDraftManager {
    private final List<OrderDraftLine> lines = new ArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public synchronized void addProduct(ProductInventoryItem item) {
        if (item == null) {
            return;
        }
        BigDecimal defaultQty = normalizeDefaultQty(item.getQtySold30Days());
        for (OrderDraftLine line : lines) {
            if (line.getProductId() == item.getProductId()) {
                line.setQtyToOrder(line.getQtyToOrder().add(defaultQty));
                notifyListeners();
                return;
            }
        }
        OrderDraftLine line = new OrderDraftLine();
        line.setProductId(item.getProductId());
        line.setCategoryName(item.getCategoryName());
        line.setSku(item.getSku());
        line.setProductName(item.getProductName());
        line.setBarcode(item.getBarcodes());
        line.setUomCode(item.getUomCode());
        line.setOnhandQty(defaultZero(item.getOnhandQty()));
        line.setQtySold30Days(defaultZero(item.getQtySold30Days()));
        line.setQtyToOrder(defaultQty);
        line.setBuyingPrice(defaultZero(item.getBuyingPrice()));
        line.setSellingPrice(defaultZero(item.getSellingPrice()));
        line.setExpirySummary(item.getExpirySummary());
        lines.add(line);
        notifyListeners();
    }

    public synchronized List<OrderDraftLine> snapshot() {
        List<OrderDraftLine> copy = new ArrayList<>();
        for (OrderDraftLine line : lines) {
            copy.add(copyOf(line));
        }
        return copy;
    }


    public synchronized void replaceAll(List<OrderDraftLine> newLines) {
        lines.clear();
        if (newLines != null) {
            for (OrderDraftLine line : newLines) {
                lines.add(copyOf(line));
            }
        }
        notifyListeners();
    }

    public synchronized OrderDraftLine get(int index) {
        return lines.get(index);
    }

    public synchronized int size() {
        return lines.size();
    }

    public synchronized void removeAt(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            notifyListeners();
        }
    }

    public synchronized void clear() {
        lines.clear();
        notifyListeners();
    }

    public synchronized void updateQty(int index, BigDecimal qty) {
        if (index < 0 || index >= lines.size()) return;
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            qty = BigDecimal.ONE;
        }
        lines.get(index).setQtyToOrder(qty);
        notifyListeners();
    }

    public synchronized void updateNotes(int index, String notes) {
        if (index < 0 || index >= lines.size()) return;
        lines.get(index).setNotes(notes);
        notifyListeners();
    }

    public void addListener(Runnable listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }


    private static OrderDraftLine copyOf(OrderDraftLine line) {
        OrderDraftLine c = new OrderDraftLine();
        if (line == null) {
            return c;
        }
        c.setProductId(line.getProductId());
        c.setCategoryName(line.getCategoryName());
        c.setSku(line.getSku());
        c.setProductName(line.getProductName());
        c.setBarcode(line.getBarcode());
        c.setUomCode(line.getUomCode());
        c.setOnhandQty(line.getOnhandQty());
        c.setQtySold30Days(line.getQtySold30Days());
        c.setQtyToOrder(line.getQtyToOrder());
        c.setBuyingPrice(line.getBuyingPrice());
        c.setSellingPrice(line.getSellingPrice());
        c.setExpirySummary(line.getExpirySummary());
        c.setNotes(line.getNotes());
        return c;
    }

    private static BigDecimal normalizeDefaultQty(BigDecimal value) {
        BigDecimal normalized = defaultZero(value);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return normalized;
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
