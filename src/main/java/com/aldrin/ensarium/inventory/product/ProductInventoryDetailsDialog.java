package com.aldrin.ensarium.inventory.product;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.SwingUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;

public class ProductInventoryDetailsDialog extends JDialog {

    private final ProductInventoryDao dao = new ProductInventoryDao();
    private final ProductMonthlySalesTableModel tableModel = new ProductMonthlySalesTableModel();
    private final JTable table = new JTable(tableModel);

    private final JLabel productLabel = new JLabel();
    private final JLabel onhandLabel = new JLabel();
    private final JLabel qtySoldLabel = new JLabel();
    private final JLabel qtyReturnedLabel = new JLabel();
    private final JLabel salesCountLabel = new JLabel();
    private final JLabel grossLabel = new JLabel();
    private final JLabel discountLabel = new JLabel();
    private final JLabel taxLabel = new JLabel();
    private final JLabel netNoTaxLabel = new JLabel();
    private final JLabel netWithTaxLabel = new JLabel();
    private final JLabel costLabel = new JLabel();
    private final JLabel profitNoTaxLabel = new JLabel();
    private final JLabel profitWithTaxLabel = new JLabel();
    private final JLabel avgUnitLabel = new JLabel();

    private final JComboBox<Month> monthCombo = new JComboBox<>(Month.values());
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(java.time.LocalDate.now().getYear(), 2000, 2100, 1));
    private long productId;
    private String productName;

    private JPanel centerPanel = new JPanel(new BorderLayout());
    private JPanel eastPanel = new JPanel();
    private JPanel westPanel = new JPanel();
    private JPanel southPanel = new JPanel();
    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);
    
    private final Icon iconLoadData = FaSwingIcons.icon(FontAwesomeIcon.DOWNLOAD, 24, Color.WHITE);

    public ProductInventoryDetailsDialog(java.awt.Window owner) {
        super(owner, "Product Monthly Details", ModalityType.APPLICATION_MODAL);
        eastPanel.setPreferredSize(new Dimension(13, 10));
        westPanel.setPreferredSize(new Dimension(13, 10));
        southPanel.setPreferredSize(new Dimension(10, 10));
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout(0, 0));

        JPanel north = new JPanel(new BorderLayout(10, 10));
        north.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthCombo.setPreferredSize(new Dimension(250, 30));
        monthCombo.setSelectedItem(java.time.LocalDate.now().getMonth());
        monthCombo.setFont(font14Plain);
        yearSpinner.setFont(font14Plain);
        yearSpinner.setPreferredSize(new Dimension(80, 30));
        StyledButton refreshButton = new StyledButton("Month");
        refreshButton.setIcon(iconLoadData);
        refreshButton.addActionListener(e -> loadData());
        JLabel m =new JLabel("Month:");
        m.setFont(font14Bold);
        filterPanel.add(m);
        filterPanel.add(monthCombo);
        JLabel y =new JLabel("Year:");
        y.setFont(font14Bold);
        filterPanel.add(y);
        filterPanel.add(yearSpinner);
        filterPanel.add(refreshButton);

        JPanel summary = new JPanel(new java.awt.GridLayout(0, 4, 8, 8));
        summary.setBorder(BorderFactory.createTitledBorder("Monthly Product Summary"));
        
        productLabel.setFont(font14Plain);
        onhandLabel.setFont(font14Plain);
        qtySoldLabel.setFont(font14Plain);
        qtyReturnedLabel.setFont(font14Plain);
        salesCountLabel.setFont(font14Plain);
        grossLabel.setFont(font14Plain);
        discountLabel.setFont(font14Plain);
        taxLabel.setFont(font14Plain);
        netNoTaxLabel.setFont(font14Plain);
        netWithTaxLabel.setFont(font14Plain);
        costLabel.setFont(font14Plain);
        profitNoTaxLabel.setFont(font14Plain);
        profitWithTaxLabel.setFont(font14Plain);
        avgUnitLabel.setFont(font14Plain);

        addPair(summary, "Product", productLabel);
        addPair(summary, "Onhand Qty", onhandLabel);
        addPair(summary, "Qty Sold", qtySoldLabel);
        addPair(summary, "Qty Returned", qtyReturnedLabel);
        addPair(summary, "Sales Count", salesCountLabel);
        addPair(summary, "Gross Sales", grossLabel);
        addPair(summary, "Discount", discountLabel);
        addPair(summary, "Tax", taxLabel);
        addPair(summary, "Net Sales (No Tax)", netNoTaxLabel);
        addPair(summary, "Net Sales (With Tax)", netWithTaxLabel);
        addPair(summary, "Cost", costLabel);
        addPair(summary, "Profit (No Tax)", profitNoTaxLabel);
        addPair(summary, "Profit (With Tax)", profitWithTaxLabel);
        addPair(summary, "Avg Unit Price", avgUnitLabel);

        north.add(filterPanel, BorderLayout.NORTH);
        north.add(summary, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        BootstrapTableStyle.install(table);
        int[] widths = {160, 120, 160, 180, 90, 100, 100, 100, 90, 100, 100, 180, 180};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.setColumnLeft(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        for (int i = 4; i < widths.length; i++) {
            BootstrapTableStyle.setColumnRight(table, i);
        }

        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        centerPanel.add(eastPanel,BorderLayout.EAST);
        centerPanel.add(westPanel,BorderLayout.WEST);
        centerPanel.add(southPanel,BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        

        setPreferredSize(new Dimension(1400, 760));
        pack();
        setLocationRelativeTo(getParent());
    }



    private void addPair(JPanel panel, String label, JLabel value) {
        JLabel l = new JLabel();
        l.setText(label + ":");
        l.setFont(font14Bold);
        panel.add(l);
        panel.add(value);
    }

    public void open(long productId, String productName) {
        this.productId = productId;
        this.productName = productName;
        productLabel.setText(productName);
        loadData();
        setVisible(true);
    }

    private void loadData() {
        try {
            YearMonth month = YearMonth.of((Integer) yearSpinner.getValue(), ((Month) monthCombo.getSelectedItem()).getValue());
            ProductMonthlySummary summary = dao.findMonthlySummary(productId, month);
            List<ProductMonthlySalesRow> rows = dao.findMonthlySaleRows(productId, month);
            tableModel.setRows(rows);
            productLabel.setText(productName);
            onhandLabel.setText(SwingUtils.formatQty(summary.getOnhandQty()));
            qtySoldLabel.setText(SwingUtils.formatQty(summary.getQtySold()));
            qtyReturnedLabel.setText(SwingUtils.formatQty(summary.getQtyReturned()));
            salesCountLabel.setText(String.valueOf(summary.getSalesCount()));
            grossLabel.setText(SwingUtils.formatMoney(summary.getGrossSales()));
            discountLabel.setText(SwingUtils.formatMoney(summary.getDiscountTotal()));
            taxLabel.setText(SwingUtils.formatMoney(summary.getTaxTotal()));
            netNoTaxLabel.setText(SwingUtils.formatMoney(summary.getNetSalesWithoutTax()));
            netWithTaxLabel.setText(SwingUtils.formatMoney(summary.getNetSalesWithTax()));
            costLabel.setText(SwingUtils.formatMoney(summary.getCostTotal()));
            profitNoTaxLabel.setText(SwingUtils.formatMoney(summary.getProfitWithoutTax()));
            profitWithTaxLabel.setText(SwingUtils.formatMoney(summary.getProfitWithTax()));
            avgUnitLabel.setText(SwingUtils.formatMoney(summary.getAverageUnitPrice()));
        } catch (Exception ex) {
            SwingUtils.showError(this, "Failed to load product monthly details.", ex instanceof Exception ? (Exception) ex : null);
        }
    }
}
