package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class TaxSummaryTabPanel extends JPanel {

    public TaxSummaryTabPanel(ReportService service) {
        setLayout(new BorderLayout());
        add(new ReportPanel(
                "BIR Tax Summary",
                "tax_summary.jrxml",
                (Date from, Date to) -> {
                    try {
                        return service.loadTaxSummary(from, to);
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
        map.put("Vatable Sales", "Grand Total Vatable Sales:");
        map.put("VAT Amount", "Grand Total VAT Amount:");
        map.put("VAT Exempt", "Grand Total VAT Exempt:");
        map.put("Zero Rated", "Grand Total Zero Rated:");
        map.put("Withholding Tax", "Grand Total Withholding Tax:");
        map.put("Discount", "Grand Total Discount:");
        return map;
    }
}
