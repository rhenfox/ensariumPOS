package com.aldrin.ensarium.report;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("BIR Reports / POS Profit / Financial Statement (Views Only)");
        initUi();
    }

    private void initUi() {
        ReportService service = new ReportService();
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("BIR Invoice List", new BirInvoiceListTabPanel(service));
        tabs.addTab("Tax Summary", new TaxSummaryTabPanel(service));
        tabs.addTab("Monthly Machine", new MonthlyMachineTabPanel(service));
        tabs.addTab("POS Profit", new PosProfitTabPanel(service));
        tabs.addTab("Financial Statement", new FinancialStatementTabPanel(service));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 780);
        setLocationRelativeTo(null);
    }
}
