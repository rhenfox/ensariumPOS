package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import java.util.Date;
import javax.swing.JPanel;

public class MonthlyMachineTabPanel extends JPanel {

    public MonthlyMachineTabPanel(ReportService service) {
        setLayout(new BorderLayout());
        add(new ReportPanel(
                "BIR Monthly Machine Sales",
                "monthly_sales_machine.jrxml",
                (Date from, Date to) -> {
                    try {
                        return service.loadMonthlyMachineSales(from, to);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                service), BorderLayout.CENTER);
    }
}
