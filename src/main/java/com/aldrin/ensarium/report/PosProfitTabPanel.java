package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class PosProfitTabPanel extends JPanel {

    public PosProfitTabPanel(ReportService service) {
        setLayout(new BorderLayout());
        add(new ReportPanel(
                "POS Profit Report",
                "pos_profit.jrxml",
                (Date from, Date to) -> {
                    try {
                        return service.loadPosProfit(from, to);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                service,
                totals(),
                "View Sale Details",
                (parent, panel, modelRow) -> service.showSaleDetails(parent, panel.getLongModelValue(modelRow, "__SALE_ID")),
                true), BorderLayout.CENTER);
    }

    private static Map<String, String> totals() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Gross Amount", "Grand Total Gross Amount:");
        map.put("Discount", "Grand Total Discount:");
        map.put("Tax Total", "Grand Total Tax Total:");
        map.put("Net Sales", "Grand Total Net Sales:");
        map.put("COGS", "Grand Total COGS:");
        map.put("Gross Profit", "Grand Total Gross Profit:");
        return map;
    }
}
