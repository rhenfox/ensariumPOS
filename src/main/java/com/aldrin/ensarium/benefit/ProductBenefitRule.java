package com.aldrin.ensarium.benefit;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.icons.Icons;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
//import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import com.toedter.calendar.JDateChooser;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Icon;

public class ProductBenefitRule extends JPanel {

    private final ProductBenefitRuleService service = new ProductBenefitRuleService();
    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    Color color = Color.WHITE;

    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, Color.WHITE);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, Color.WHITE);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH, 24, Color.WHITE);
    Icon iconResetPassword = new Icons().key(18, Color.WHITE);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);

    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, Color.WHITE);
    Icon iconCancel = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, Color.WHITE);

    public ProductBenefitRule() {
        setLayout(new BorderLayout());

        ProductBenefitRuleTab content = new ProductBenefitRuleTab();
        add(content, BorderLayout.CENTER);

        content.refresh();
    }

    private abstract class BaseTab extends JPanel {

        final JTextField txtSearch = new JTextField(22);
        final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));

        BaseTab() {
            setLayout(new BorderLayout(0, 10));
            setBorder(new EmptyBorder(6, 6, 6, 6));
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    applyFilter();
                }
            });
            spnLimit.addChangeListener(e -> applyFilter());
        }

        abstract void refresh();

        abstract void applyFilter();

        abstract List<String> suggestions();

        void installSuggest() {
            AutoSuggestSupport.install(txtSearch, this::suggestions);
        }

        boolean match(String haystack, String q) {
            return q.isEmpty() || haystack.toLowerCase(Locale.ROOT).contains(q);
        }

        int limit() {
            return (Integer) spnLimit.getValue();
        }

        String query() {
            return txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        }
    }

    private final class ProductBenefitRuleTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "ID", "Product", "Benefit Type", "Benefit Mode", "VAT Exempt", "Active", "Effective From", "Effective To"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        final JTable table = new JTable(model);

        final List<ProductBenefitRuleRow> allRows = new ArrayList<>();

        ProductBenefitRuleTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");

            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnRefresh.setIcon(iconRefresh);

//            TableStyleSupport.apply(table);
//
//            table.getColumnModel().getColumn(1).setPreferredWidth(320);
//            table.getColumnModel().getColumn(2).setPreferredWidth(120);
//            table.getColumnModel().getColumn(3).setPreferredWidth(150);
//            table.getColumnModel().getColumn(4).setPreferredWidth(100);
//            table.getColumnModel().getColumn(5).setPreferredWidth(80);
//            table.getColumnModel().getColumn(6).setPreferredWidth(120);
//            table.getColumnModel().getColumn(7).setPreferredWidth(120);

//            table.setAutoCreateRowSorter(true);
//            TableStyleSupport.apply(table);
//
//            centerHeader(table, 2);
//            centerHeader(table, 3);
//            centerHeader(table, 4);
//            centerHeader(table, 5);
//            centerHeader(table, 6);
//            centerHeader(table, 7);

//            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
//            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
//            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
//            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
//            table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
//            table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
//            table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
//
//            hideColumn(table, 0);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {0, 300, 120, 150, 150,150,150,150};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setColumnLeft(table, 5);
            BootstrapTableStyle.setColumnLeft(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);
            
            
            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new RoundedScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());

            installSuggest();
        }

        @Override
        public void refresh() {
            allRows.clear();
            allRows.addAll(service.listProductBenefitRules());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedLongId(table, model);
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (ProductBenefitRuleRow r : allRows) {
                String hay = (nv(r.getProductSku()) + " "
                        + nv(r.getProductName()) + " "
                        + nv(r.getBenefitType()) + " "
                        + nv(r.getBenefitMode()) + " "
                        + (r.isVatExempt() ? "vat exempt yes" : "vat exempt no") + " "
                        + (r.isActive() ? "active yes" : "active no") + " "
                        + nv(r.getEffectiveFrom() == null ? null : r.getEffectiveFrom().toString()) + " "
                        + nv(r.getEffectiveTo() == null ? null : r.getEffectiveTo().toString())).toLowerCase(Locale.ROOT);
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{
                    r.getId(),
                    productLabel(r),
                    r.getBenefitType(),
                    r.getBenefitMode(),
                    r.isVatExempt() ? "Yes" : "No",
                    r.isActive() ? "Yes" : "No",
                    r.getEffectiveFrom(),
                    r.getEffectiveTo()
                });
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (ProductBenefitRuleRow r : allRows) {
                out.add(r.getProductSku());
                out.add(r.getProductName());
                out.add(r.getBenefitType());
                out.add(r.getBenefitMode());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
        }

        void onAdd() {
            ProductBenefitRuleDialog d = new ProductBenefitRuleDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createProductBenefitRule(
                        d.productId(),
                        d.benefitType(),
                        d.benefitMode(),
                        d.vatExempt(),
                        d.active(),
                        d.effectiveFrom(),
                        d.effectiveTo()
                );
                refresh();
            } catch (Exception ex) {
                SwingUtils.error(ProductBenefitRule.this, "Failed to create product benefit rule.", ex);
            }
        }

        void onEdit() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductBenefitRule.this, "Select a product benefit rule first.");
                return;
            }
            ProductBenefitRuleRow row = allRows.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            ProductBenefitRuleDialog d = new ProductBenefitRuleDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateProductBenefitRule(
                        id,
                        d.productId(),
                        d.benefitType(),
                        d.benefitMode(),
                        d.vatExempt(),
                        d.active(),
                        d.effectiveFrom(),
                        d.effectiveTo()
                );
                refresh();
            } catch (Exception ex) {
                SwingUtils.error(ProductBenefitRule.this, "Failed to update product benefit rule.", ex);
            }
        }

        void onDelete() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductBenefitRule.this, "Select a product benefit rule first.");
                return;
            }
            if (!SwingUtils.confirm(ProductBenefitRule.this, "Delete selected product benefit rule?")) {
                return;
            }
            try {
                service.deleteProductBenefitRule(id);
                refresh();
            } catch (Exception ex) {
                SwingUtils.error(ProductBenefitRule.this, "Failed to delete product benefit rule.", ex);
            }
        }

        final class ProductBenefitRuleDialog extends JDialog {

            final JComboBox<LookupOption> cboProduct = new JComboBox<>();
//            final JComboBox<String> cboBenefitType = new JComboBox<>(new String[]{"SENIOR", "PWD"});
            final JComboBox<String> cboBenefitType = new JComboBox<>();
            final JComboBox<String> cboBenefitMode = new JComboBox<>(new String[]{"NONE", "DISCOUNT_20", "DISCOUNT_5_BNPC"});
            final JCheckBox chkVatExempt = new JCheckBox("VAT Exempt");
            final JCheckBox chkActive = new JCheckBox("Active", true);
            final JDateChooser dcEffectiveFrom = new JDateChooser();
            final JDateChooser dcEffectiveTo = new JDateChooser();
            boolean saved;

            ProductBenefitRuleDialog(ProductBenefitRuleRow row) {
                super(ownerForDialog(ProductBenefitRule.this), row == null ? "Add Product Benefit Rule" : "Edit Product Benefit Rule", ModalityType.APPLICATION_MODAL);

                chkVatExempt.setOpaque(false);
                chkActive.setOpaque(false);

                DefaultComboBoxModel<LookupOption> productModel = new DefaultComboBoxModel<>();
                for (LookupOption o : service.productOptions()) {
                    productModel.addElement(o);
                }
                cboProduct.setModel(productModel);


                loadBenefitTypeItems(cboBenefitType, row == null ? null : row.getBenefitType());

                applyComboBoxAutoFill(cboProduct);
                applyComboBoxAutoFill(cboBenefitMode);

                dcEffectiveFrom.setDateFormatString("yyyy-MM-dd");
                dcEffectiveTo.setDateFormatString("yyyy-MM-dd");

                if (row != null) {
                    selectLookup(cboProduct, row.getProductId());
                    Object selected = cboProduct.getSelectedItem();
                    if (selected != null) {
                        JTextComponent editor = (JTextComponent) cboProduct.getEditor().getEditorComponent();
                        editor.setText(selected.toString());
                    }
                    selectStringIgnoreCase(cboBenefitType, row.getBenefitType());
                    cboBenefitMode.setSelectedItem(row.getBenefitMode());
                    chkVatExempt.setSelected(row.isVatExempt());
                    chkActive.setSelected(row.isActive());
                    dcEffectiveFrom.setDate(localDateToDate(row.getEffectiveFrom()));
                    dcEffectiveTo.setDate(localDateToDate(row.getEffectiveTo()));
                } else {
                    cboBenefitType.setSelectedIndex(0);
                    dcEffectiveFrom.setDate(localDateToDate(LocalDate.now()));
                    dcEffectiveTo.setDate(null);
                }

                cboProduct.setFont(font14Plain);
                cboProduct.setPreferredSize(new Dimension(250, 30));

                cboBenefitType.setFont(font14Plain);
                cboBenefitType.setPreferredSize(new Dimension(250, 30));

                cboBenefitMode.setFont(font14Plain);
                cboBenefitMode.setPreferredSize(new Dimension(250, 30));

                chkVatExempt.setFont(font14Plain);
                chkVatExempt.setPreferredSize(new Dimension(250, 30));

                chkActive.setFont(font14Plain);
                chkActive.setPreferredSize(new Dimension(250, 30));

                dcEffectiveFrom.setFont(font14Plain);
                dcEffectiveFrom.setPreferredSize(new Dimension(250, 30));

                dcEffectiveTo.setFont(font14Plain);
                dcEffectiveTo.setPreferredSize(new Dimension(250, 30));

                JPanel form = simpleForm(
                        new String[]{"Product", "Benefit Type", "Benefit Mode", "VAT Exempt", "Status", "Effective From", "Effective To"},
                        new JComponent[]{cboProduct, cboBenefitType, cboBenefitMode, chkVatExempt, chkActive, dcEffectiveFrom, dcEffectiveTo}
                );

                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setIcon(iconCancel);
                cancel.setDanger();

                ok.addActionListener(e -> {
                    if (!(cboProduct.getSelectedItem() instanceof LookupOption)) {
                        SwingUtils.info(this, "Product is required.");
                        return;
                    }
                    try {
                        LocalDate from = effectiveFrom();
                        LocalDate to = effectiveTo();
                        if (to != null && to.isBefore(from)) {
                            SwingUtils.info(this, "Effective To cannot be earlier than Effective From.");
                            return;
                        }
                        saved = true;
                        dispose();
                    } catch (Exception ex) {
                        SwingUtils.info(this, "Effective From is required. Use the calendar fields for the dates.");
                    }
                });

                cancel.addActionListener(e -> dispose());

                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(ProductBenefitRule.this);
            }

            long productId() {
                return ((LookupOption) cboProduct.getSelectedItem()).getId();
            }

            String benefitType() {
                return String.valueOf(cboBenefitType.getSelectedItem()).trim();
            }

            String benefitMode() {
                return String.valueOf(cboBenefitMode.getSelectedItem()).trim();
            }

            boolean vatExempt() {
                return chkVatExempt.isSelected();
            }

            boolean active() {
                return chkActive.isSelected();
            }

            LocalDate effectiveFrom() {
                if (dcEffectiveFrom.getDate() == null) {
                    throw new IllegalArgumentException("Effective From is required.");
                }
                return dateToLocalDate(dcEffectiveFrom.getDate());
            }

            LocalDate effectiveTo() {
                if (dcEffectiveTo.getDate() == null) {
                    return null;
                }
                return dateToLocalDate(dcEffectiveTo.getDate());
            }
        }
    }

//    private static void selectStringIgnoreCase(JComboBox<String> combo, String value) {
//        if (value == null) {
//            return;
//        }
//        String wanted = value.trim();
//        for (int i = 0; i < combo.getItemCount(); i++) {
//            String item = combo.getItemAt(i);
//            if (item != null && item.trim().equalsIgnoreCase(wanted)) {
//                combo.setSelectedIndex(i);
//                return;
//            }
//        }
//
//        // if not found in model, add it so edit still shows DB value
//        combo.addItem(wanted);
//        combo.setSelectedItem(wanted);
//    }
    private static String productLabel(ProductBenefitRuleRow r) {
        String sku = nv(r.getProductSku());
        String name = nv(r.getProductName());
        if (!sku.isEmpty()) {
            return sku + " - " + name;
        }
        return name;
    }
//     Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
//     Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    private static JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, StyledButton... buttons) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
        Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        left.add(searchl);
        txtSearch.setFont(font14Plain);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
        txtSearch.setPreferredSize(new Dimension(250, 30));
        left.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(font14Plain);
        left.add(limitl);
        spnLimit.setFont(font14Plain);
        left.add(spnLimit);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (StyledButton button : buttons) {
            right.add(button);
        }

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.EAST);
        return root;
    }

    private static JPanel simpleForm(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
        Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(font14Bold);
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(fields[i], gbc);
        }
        return panel;
    }

    private static JPanel buttonBar(StyledButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        for (StyledButton button : buttons) {
            p.add(button);
        }
        return p;
    }

    private static Long selectedLongId(JTable table, DefaultTableModel model) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = model.getValueAt(modelRow, 0);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private static void reselect(JTable table, DefaultTableModel model, Long keepId) {
        if (keepId == null) {
            return;
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 0);
            if (value != null && keepId.equals(Long.valueOf(String.valueOf(value)))) {
                int viewRow = table.convertRowIndexToView(i);
                table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    private static void hideColumn(JTable table, int index) {
        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
    }

    private static void centerHeader(JTable table, int column) {
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(column).setHeaderRenderer(renderer);
    }

    private static String nv(String text) {
        return text == null ? "" : text;
    }

    private static <T extends LookupOption> void selectLookup(JComboBox<T> combo, long id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            T item = combo.getItemAt(i);
            if (item != null && item.getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }

    private static java.util.Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate dateToLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Window ownerForDialog(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        if (w != null) {
            return w;
        }
        return new Frame();
    }

    private static final class RoundedScrollPane extends JScrollPane {

        RoundedScrollPane(Component view) {
            super(view);
            setBorder(BorderFactory.createLineBorder(getBackground().darker()));
        }
    }

    private static final class TableStyleSupport {

        static void apply(JTable table) {
            table.setRowHeight(26);
            table.setFillsViewportHeight(true);
            table.getTableHeader().setReorderingAllowed(false);
        }
    }

    private static final class SwingUtils {

        static void info(Component parent, String message) {
            JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        }

        static boolean confirm(Component parent, String message) {
            return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        }

        static void error(Component parent, String message, Exception ex) {
            String detail = ex.getMessage();
            if (detail == null || detail.isBlank()) {
                detail = ex.toString();
            }
            JOptionPane.showMessageDialog(parent, message + "\n\n" + detail, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class AutoSuggestSupport {

        private AutoSuggestSupport() {
        }

        static void install(JTextField field, SuggestionProvider provider) {
            field.putClientProperty("suggestions", provider);
        }
    }

    @FunctionalInterface
    private interface SuggestionProvider {

        List<String> get();
    }

    private static void loadBenefitTypeItems(JComboBox<String> combo, String selectedValue) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("SENIOR");
        model.addElement("PWD");

        if (selectedValue != null && !selectedValue.trim().isEmpty() && !containsIgnoreCase(model, selectedValue)) {
            model.addElement(selectedValue.trim());
        }

        combo.setModel(model);
        selectStringIgnoreCase(combo, selectedValue);
    }

    private static boolean containsIgnoreCase(DefaultComboBoxModel<String> model, String value) {
        String wanted = value == null ? "" : value.trim();
        for (int i = 0; i < model.getSize(); i++) {
            String item = model.getElementAt(i);
            if (item != null && item.trim().equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }

    private static void selectStringIgnoreCase(JComboBox<String> combo, String value) {
        if (value == null) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
            return;
        }

        String wanted = value.trim();
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.trim().equalsIgnoreCase(wanted)) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        combo.setSelectedItem(wanted);
    }

}
