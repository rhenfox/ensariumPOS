package com.aldrin.ensarium.report;


import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class BirInvoiceListTabPanel extends JPanel {

    public BirInvoiceListTabPanel(ReportService service) {
        setLayout(new BorderLayout());
        add(new ReportPanel(
                "BIR Invoice List",
                "bir_invoice_list.jrxml",
                (Date from, Date to) -> {
                    try {
                        return service.loadBirInvoiceList(from, to);
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
        map.put("Gross Sales", "Grand Total Gross Sales:");
        map.put("Vatable Sales", "Grand Total Vatable Sales:");
        map.put("VAT Amount", "Grand Total VAT Amount:");
        map.put("VAT Exempt", "Grand Total VAT Exempt:");
        return map;
    }
}
