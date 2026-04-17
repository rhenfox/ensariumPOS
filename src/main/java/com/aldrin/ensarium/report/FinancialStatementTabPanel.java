package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class FinancialStatementTabPanel extends JPanel {

    public FinancialStatementTabPanel(ReportService service) {
        setLayout(new BorderLayout());
        add(new ReportPanel(
                "Financial Statement",
                "financial_statement.jrxml",
                (Date from, Date to) -> {
                    try {
                        return service.loadFinancialStatement(from, to);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                service,
                totals()), BorderLayout.CENTER);
    }

    private static Map<String, String> totals() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Gross Sales", "Grand Total Gross Sales:");
        map.put("Sales Discount", "Grand Total Sales Discount:");
        map.put("Sales Net", "Grand Total Sales Net:");
        map.put("Output VAT", "Grand Total Output VAT:");
        map.put("Sales Returns", "Grand Total Sales Returns:");
        map.put("Net Sales After Returns", "Grand Total Net Sales After Returns:");
        map.put("Net COGS", "Grand Total Net COGS:");
        map.put("Gross Profit", "Grand Total Gross Profit:");
        map.put("Purchases", "Grand Total Purchases:");
        map.put("Return Cost", "Grand Total Return Cost:");
        map.put("Damage", "Grand Total Damage:");
        map.put("Expire", "Grand Total Expire:");
        map.put("Adjustment", "Grand Total Adjustment:");
        return map;
    }
}
