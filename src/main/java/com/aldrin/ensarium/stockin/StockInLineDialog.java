package com.aldrin.ensarium.stockin;


import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import com.toedter.calendar.JDateChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class StockInLineDialog extends JDialog {

    private final StockInDao stockInDao = new StockInDao();
    private final JComboBox<ProductOption> cboProduct = new JComboBox<>();
    private final JTextField txtLotNo = new JTextField(20);
    private final JDateChooser dcExpiry = new JDateChooser();
    private final JTextField txtUnit = new JTextField(10);
    private final JTextField txtQty = new JTextField(12);
    private final JTextField txtUnitCost = new JTextField(12);
    private final StockInLine line;
    private BigDecimal autoFilledUnitCost;
    private boolean saved;
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);

//    public StockInLineDialog(Frame owner, StockInLine line) {
//        super(owner, "Stock-In Line", true);
//        this.line = line;
//        initUi();
//        loadProducts();
//        bindLine();
//    }
    
        public StockInLineDialog(java.awt.Window owner, StockInLine line) {
//        super(owner, "Stock-In Line", true);
        super(owner, "Stock-In Line", Dialog.ModalityType.APPLICATION_MODAL);
        this.line = line;
        initUi();
        loadProducts();
        bindLine();
        applyComboBoxAutoFill(cboProduct);
//        txtBarcode.putClientProperty("JTextField.placeholderText", "Enter barcode");

//.setFont(new java.awt.Font("Segoe UI", 1, 14)); 


    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtUnit.setEditable(false);

        int row = 0;
        
        JLabel productl = new JLabel("Product");
        productl.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
        gbc.gridx = 0; gbc.gridy = row; form.add(productl, gbc);
        

        cboProduct.setPreferredSize(new Dimension(200, 30));
        cboProduct.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        gbc.gridx = 1; form.add(cboProduct, gbc);

        row++;
        JLabel lotl = new JLabel("Lot No");
        lotl.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
        gbc.gridx = 0; gbc.gridy = row; form.add(lotl, gbc);
        txtLotNo.setPreferredSize(new Dimension(200, 30));
        txtLotNo.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        txtLotNo.putClientProperty("JTextField.placeholderText", "Enter lot no");
        gbc.gridx = 1; form.add(txtLotNo, gbc);

        dcExpiry.setDateFormatString("yyyy-MM-dd");

        row++;
        JLabel expiryDatel = new JLabel("Expiry Date");
        expiryDatel.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
        gbc.gridx = 0; gbc.gridy = row; form.add(expiryDatel, gbc);
        dcExpiry.setPreferredSize(new Dimension(200, 30));
        dcExpiry.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        gbc.gridx = 1; form.add(dcExpiry, gbc);

        row++;
        JLabel unitl = new JLabel("Expiry Date");
        unitl.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
        gbc.gridx = 0; gbc.gridy = row; form.add(unitl, gbc);
        txtUnit.setPreferredSize(new Dimension(200, 30));
        txtUnit.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        gbc.gridx = 1; form.add(txtUnit, gbc);

        row++;
        JLabel qtyBasel = new JLabel("Qty Base");
        qtyBasel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        gbc.gridx = 0; gbc.gridy = row; form.add(qtyBasel, gbc);
        txtQty.setPreferredSize(new Dimension(200, 30));
        txtQty.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        gbc.gridx = 1; form.add(txtQty, gbc);

        row++;
        JLabel costl = new JLabel("Unit Cost");
        costl.setFont(new java.awt.Font("Segoe UI", 1, 14));
        gbc.gridx = 0; gbc.gridy = row; form.add(costl, gbc);
        txtUnitCost.setPreferredSize(new Dimension(200, 30));
        txtUnitCost.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        gbc.gridx = 1; form.add(txtUnitCost, gbc);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        StyledButton btnSave = new StyledButton("Save");
        StyledButton btnCancel = new StyledButton("Cancel");
        btnCancel.setDanger();
        btnSave.setIcon(iconSave);
        btnCancel.setIcon(iconCancel);
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        actions.add(btnCancel);
        actions.add(btnSave);
        add(actions, BorderLayout.SOUTH);

        cboProduct.addActionListener(e -> onProductChanged());

        setSize(560, 320);
        setLocationRelativeTo(getOwner());
    }

    private void loadProducts() {
        try {
            cboProduct.addItem(new ProductOption());
            List<ProductOption> products = stockInDao.listProducts();
            for (ProductOption product : products) {
                cboProduct.addItem(product);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Products", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bindLine() {
        if (line.getProduct() != null && line.getProduct().getId() != null) {
            for (int i = 0; i < cboProduct.getItemCount(); i++) {
                ProductOption item = cboProduct.getItemAt(i);
                if (item != null && line.getProduct().getId().equals(item.getId())) {
                    cboProduct.setSelectedIndex(i);
                    break;
                }
            }
        } else if (cboProduct.getItemCount() > 0) {
            cboProduct.setSelectedIndex(0);
        }
        txtLotNo.setText(line.getLotNo() == null ? "" : line.getLotNo());
        dcExpiry.setDate(line.getExpiryDate());
        txtQty.setText(line.getQuantityInBase() == null ? "" : line.getQuantityInBase().toPlainString());
        txtUnitCost.setText(line.getUnitCost() == null ? "" : line.getUnitCost().toPlainString());
        autoFilledUnitCost = line.getUnitCost();
        updateUnitField();
    }

    private void updateUnitField() {
        ProductOption product = (ProductOption) cboProduct.getSelectedItem();
        txtUnit.setText(product == null ? "" : product.getBaseUomCode());
    }

    private void onProductChanged() {
        updateUnitField();
        ProductOption product = (ProductOption) cboProduct.getSelectedItem();
        if (product == null || product.getId() == null) {
            return;
        }
        try {
            BigDecimal previousUnitCost = stockInDao.findLatestUnitCost(product.getId());
            String currentText = txtUnitCost.getText() == null ? "" : txtUnitCost.getText().trim();
            boolean replaceValue = currentText.isEmpty();
            if (!replaceValue && autoFilledUnitCost != null) {
                try {
                    BigDecimal currentValue = new BigDecimal(currentText);
                    replaceValue = currentValue.compareTo(autoFilledUnitCost) == 0;
                } catch (NumberFormatException ignored) {
                    replaceValue = false;
                }
            }
            if (replaceValue) {
                txtUnitCost.setText(previousUnitCost.stripTrailingZeros().toPlainString());
            }
            autoFilledUnitCost = previousUnitCost;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Previous Unit Cost", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        try {
            ProductOption product = (ProductOption) cboProduct.getSelectedItem();
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("Product is required.");
            }
            BigDecimal qty = new BigDecimal(txtQty.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            BigDecimal unitCost = new BigDecimal(txtUnitCost.getText().trim());
            if (unitCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit cost cannot be negative.");
            }
            line.setProduct(product);
            line.setUnit(new LookupOption(product.getBaseUomId(), product.getBaseUomCode(), product.getBaseUomCode()));
            line.setLotNo(blankToNull(txtLotNo.getText()));
            line.setExpiryDate(dcExpiry.getDate());
            line.setQuantityInBase(qty);
            line.setUnitCost(unitCost);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Line", JOptionPane.ERROR_MESSAGE);
        }
    }


    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    public boolean isSaved() {
        return saved;
    }
    
        private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }
}
