package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.benefit.ProductBenefitRule;
import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.*;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.ProductAdminService;
import com.aldrin.ensarium.ui.widgets.BootstrapTabbedPaneStyle;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.util.ComboBoxAutoFill;
import com.aldrin.ensarium.util.ComboBoxAutoSuggestSupport;
import com.aldrin.ensarium.util.SwingUtils;
import com.aldrin.ensarium.util.TableStyleSupport;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
//import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
//import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

public class ProductsPanel extends JPanel {

    private final Session session;
    private final ProductAdminService service = new ProductAdminService();
    private final boolean canWrite;

    private final ProductTab productTab;
    private final CategoryTab categoryTab;
    private final UnitTab unitTab;
    private final TaxTab taxTab;
    private final BarcodeTab barcodeTab;
    private final ProductBenefitRule productBenefitRulePanel;

    Color color = Color.WHITE;
    Icon iconAdd = FaSwingIcons.icon(FontAwesomeIcon.PLUS, 24, color);
    Icon iconEdit = FaSwingIcons.icon(FontAwesomeIcon.EDIT, 24, color);
    Icon iconDelete = FaSwingIcons.icon(FontAwesomeIcon.TRASH_ALT, 24, color);
    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, color);
    Icon iconSave = FaSwingIcons.icon(FontAwesomeIcon.SAVE, 24, color);
    Icon iconClose = FaSwingIcons.icon(FontAwesomeIcon.CLOSE, 24, color);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public ProductsPanel(Session session) {
        this.session = session;
        this.canWrite = service.isAdminUser(session.userId());

        this.productTab = new ProductTab();
        this.categoryTab = new CategoryTab();
        this.unitTab = new UnitTab();
        this.taxTab = new TaxTab();
        this.barcodeTab = new BarcodeTab();
        this.productBenefitRulePanel = new ProductBenefitRule();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Products");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Products", productTab);
        tabs.addTab("Barcodes", barcodeTab);
        tabs.addTab("Benefit", productBenefitRulePanel);
        tabs.addTab("Categories", categoryTab);
        tabs.addTab("Units", unitTab);
        tabs.addTab("Taxes", taxTab);
        BootstrapTabbedPaneStyle.Style style = BootstrapTabbedPaneStyle.Style.bootstrapDefault()
                .accent(new Color(0x0D6EFD));

        BootstrapTabbedPaneStyle.install(tabs, style);
        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    public void refreshAll() {
        categoryTab.refresh();
        unitTab.refresh();
        taxTab.refresh();
        productTab.refresh();
        barcodeTab.refresh();
    }

    private void hideColumn(JTable table, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= table.getColumnModel().getColumnCount()) {
            return;
        }
        var column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setWidth(0);
    }

    private JPanel buildToolbar(JTextField txtSearch, JSpinner spnLimit, StyledButton... rightButtons) {
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        leftTools.add(searchl);
        txtSearch.setFont(font14Plain);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");
        txtSearch.setPreferredSize(new Dimension(250, 30));
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(font14Bold);
        leftTools.add(limitl);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setFont(font14Plain);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (StyledButton b : rightButtons) {
            rightTools.add(b);
        }

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        return toolbar;
    }

    private abstract class BaseTab extends JPanel {

        final JTextField txtSearch = new JTextField(22);
        final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(50, 1, 10000, 1));

        BaseTab() {
            setLayout(new BorderLayout(0, 10));
            setBorder(new EmptyBorder(6, 0, 0, 0));
            txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    applyFilter();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
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
            return q.isEmpty() || haystack.toLowerCase().contains(q);
        }

        int limit() {
            return (Integer) spnLimit.getValue();
        }

        String query() {
            return txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        }
    }

    private final class CategoryTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Category", "Parent"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        final List<CategoryRow> allRows = new ArrayList<>();

        CategoryTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 350, 350};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            for (int i = 6; i <= 13; i++) {
                BootstrapTableStyle.setColumnRight(table, i);
            }

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listCategories());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedId();
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (CategoryRow row : allRows) {
                String hay = (row.name() + " " + nv(row.parentName())).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{row.id(), row.name(), row.parentName()});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (CategoryRow r : allRows) {
                out.add(r.name());
                out.add(r.parentName());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        Integer selectedId() {
            return selectedIntId(table, model);
        }

        void onAdd() {
            CategoryDialog d = new CategoryDialog(null, null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createCategory(session.userId(), d.name(), d.parentId());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to create category.", (Exception) ex);
            }
        }

        void onEdit() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a category first.");
                return;
            }
            CategoryRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            CategoryDialog d = new CategoryDialog(row, id);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateCategory(session.userId(), id, d.name(), d.parentId());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to update category.", (Exception) ex);
            }
        }

        void onDelete() {
            Integer id = selectedId();
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a category first.");
                return;
            }
            if (!SwingUtils.confirm(ProductsPanel.this, "Delete selected category?")) {
                return;
            }
            try {
                service.deleteCategory(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to delete category.", (Exception) ex);
            }
        }

        final class CategoryDialog extends JDialog {

            final JTextField txtName = new JTextField(20);
            final JComboBox<Object> cboParent = new JComboBox<>();
            boolean saved;
            final Integer editingId;

            CategoryDialog(CategoryRow row, Integer editingId) {
                super(SwingUtilities.getWindowAncestor(ProductsPanel.this), row == null ? "Add Category" : "Edit Category", ModalityType.APPLICATION_MODAL);
                this.editingId = editingId;
                cboParent.addItem("(None)");
                for (CategoryRow c : service.listCategories()) {
                    if (editingId != null && c.id() == editingId) {
                        continue;
                    }
                    cboParent.addItem(new LookupOption(c.id(), c.name()));
                }
                ComboBoxAutoSuggestSupport.install(cboParent);
                if (row != null) {
                    txtName.setText(row.name());
                    selectLookup(cboParent, row.parentId());
                }
                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));

                cboParent.setFont(font14Plain);
                cboParent.setPreferredSize(new Dimension(250, 30));
                JPanel form = simpleForm(new String[]{"Name", "Parent"}, new JComponent[]{txtName, cboParent});
                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setIcon(iconClose);
                cancel.setDanger();
                ok.addActionListener(e -> {
                    if (name().isBlank()) {
                        SwingUtils.info(this, "Name is required.");
                        return;
                    }
                    saved = true;
                    dispose();
                });
                cancel.addActionListener(e -> dispose());
                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(ProductsPanel.this);
            }

            String name() {
                return txtName.getText().trim();
            }

            Integer parentId() {
                Object o = cboParent.getSelectedItem();
                return o instanceof LookupOption lo ? lo.id() : null;
            }
        }
    }

    private final class UnitTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Code", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        final List<UnitRow> allRows = new ArrayList<>();

        UnitTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 150, 350};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            for (int i = 6; i <= 13; i++) {
                BootstrapTableStyle.setColumnRight(table, i);
            }

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listUnits());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedIntId(table, model);
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (UnitRow r : allRows) {
                String hay = (r.code() + " " + r.name()).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{r.id(), r.code(), r.name()});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (UnitRow r : allRows) {
                out.add(r.code());
                out.add(r.name());
            }
            return new ArrayList<>(out);
        }

        void onAdd() {
            UnitDialog d = new UnitDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createUnit(session.userId(), d.code(), d.name());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to create unit.", (Exception) ex);
            }
        }

        void onEdit() {
            Integer id = selectedIntId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a unit first.");
                return;
            }
            UnitRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            UnitDialog d = new UnitDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateUnit(session.userId(), id, d.code(), d.name());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to update unit.", (Exception) ex);
            }
        }

        void onDelete() {
            Integer id = selectedIntId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a unit first.");
                return;
            }
            if (!SwingUtils.confirm(ProductsPanel.this, "Delete selected unit?")) {
                return;
            }
            try {
                service.deleteUnit(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to delete unit.", (Exception) ex);
            }
        }

        final class UnitDialog extends JDialog {

            final JTextField txtCode = new JTextField(20);
            final JTextField txtName = new JTextField(20);
            boolean saved;

            UnitDialog(UnitRow row) {
                super(SwingUtilities.getWindowAncestor(ProductsPanel.this), row == null ? "Add Unit" : "Edit Unit", ModalityType.APPLICATION_MODAL);
                if (row != null) {
                    txtCode.setText(row.code());
                    txtName.setText(row.name());
                }
                txtCode.setFont(font14Plain);
                txtCode.putClientProperty("JTextField.placeholderText", "Code");
                txtCode.setPreferredSize(new Dimension(250, 30));

                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));
                JPanel form = simpleForm(new String[]{"Code", "Name"}, new JComponent[]{txtCode, txtName});
                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setIcon(iconClose);
                cancel.setDanger();
                ok.addActionListener(e -> {
                    if (code().isBlank() || name().isBlank()) {
                        SwingUtils.info(this, "Code and name are required.");
                        return;
                    }
                    saved = true;
                    dispose();
                });
                cancel.addActionListener(e -> dispose());
                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(ProductsPanel.this);
            }

            String code() {
                return txtCode.getText().trim();
            }

            String name() {
                return txtName.getText().trim();
            }
        }
    }

    private final class TaxTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Rate", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

        };
        final JTable table = new JTable(model);
        final List<TaxRow> allRows = new ArrayList<>();

        TaxTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {55, 150, 250, 150, 100};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnRight(table, 3);
            BootstrapTableStyle.setColumnCenter(table, 4);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderRight(table, 3);
            BootstrapTableStyle.setHeaderCenter(table, 4);
            for (int i = 6; i <= 13; i++) {
                BootstrapTableStyle.setColumnRight(table, i);
            }

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listTaxes());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Integer keepId = selectedIntId(table, model);
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (TaxRow r : allRows) {
                String hay = (r.code() + " " + r.name() + " " + r.rate() + " " + (r.active() ? "active" : "inactive")).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{r.id(), r.code(), r.name(), r.rate().setScale(2, RoundingMode.HALF_UP), r.active() ? "Yes" : "No"});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (TaxRow r : allRows) {
                out.add(r.code());
                out.add(r.name());
            }
            return new ArrayList<>(out);
        }

        void onAdd() {
            TaxDialog d = new TaxDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createTax(session.userId(), d.code(), d.name(), d.rate(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to create tax.", (Exception) ex);
            }
        }

        void onEdit() {
            Integer id = selectedIntId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a tax first.");
                return;
            }
            TaxRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            TaxDialog d = new TaxDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateTax(session.userId(), id, d.code(), d.name(), d.rate(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to update tax.", (Exception) ex);
            }
        }

        void onDelete() {
            Integer id = selectedIntId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a tax first.");
                return;
            }
            if (!SwingUtils.confirm(ProductsPanel.this, "Delete selected tax?")) {
                return;
            }
            try {
                service.deleteTax(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to delete tax.", (Exception) ex);
            }
        }

        final class TaxDialog extends JDialog {

            final JTextField txtCode = new JTextField(20);
            final JTextField txtName = new JTextField(20);
            final JTextField txtRate = new JTextField(20);
            final JCheckBox chkActive = new JCheckBox("Active", true);
            boolean saved;

            TaxDialog(TaxRow row) {
                super(SwingUtilities.getWindowAncestor(ProductsPanel.this), row == null ? "Add Tax" : "Edit Tax", ModalityType.APPLICATION_MODAL);
                chkActive.setOpaque(false);
                if (row != null) {
                    txtCode.setText(row.code());
                    txtName.setText(row.name());
                    txtRate.setText(row.rate().toPlainString());
                    chkActive.setSelected(row.active());
                } else {
                    txtRate.setText("0.12");
                }

                txtCode.setFont(font14Plain);
                txtCode.putClientProperty("JTextField.placeholderText", "Code");
                txtCode.setPreferredSize(new Dimension(250, 30));

                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));

                txtRate.setFont(font14Plain);
                txtRate.putClientProperty("JTextField.placeholderText", "Rate");
                txtRate.setPreferredSize(new Dimension(250, 30));

                chkActive.setFont(font14Plain);
                chkActive.setPreferredSize(new Dimension(250, 30));

                JPanel form = simpleForm(new String[]{"Code", "Name", "Rate", "Status"}, new JComponent[]{txtCode, txtName, txtRate, chkActive});
                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setIcon(iconClose);
                cancel.setDanger();
                ok.addActionListener(e -> {
                    if (code().isBlank() || name().isBlank()) {
                        SwingUtils.info(this, "Code and name are required.");
                        return;
                    }
                    try {
                        rate();
                        saved = true;
                        dispose();
                    } catch (Exception ex) {
                        SwingUtils.info(this, "Rate must be numeric.");
                    }
                });
                cancel.addActionListener(e -> dispose());
                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(ProductsPanel.this);
            }

            String code() {
                return txtCode.getText().trim();
            }

            String name() {
                return txtName.getText().trim();
            }

            BigDecimal rate() {
                return new BigDecimal(txtRate.getText().trim());
            }

            boolean active() {
                return chkActive.isSelected();
            }
        }
    }

    private final class ProductTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "SKU", "Name", "Category", "Unit", "Buying price", "Selling price", "Tax", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 5 || columnIndex == 6) ? BigDecimal.class : Object.class;
            }
        };
        final JTable table = new JTable(model);
        final List<ProductRow> allRows = new ArrayList<>();

        ProductTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {0, 150, 300, 140, 80, 130, 130, 100, 70};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);
            BootstrapTableStyle.setColumnRight(table, 5);
            BootstrapTableStyle.setColumnRight(table, 6);
            BootstrapTableStyle.setColumnLeft(table, 7);
            BootstrapTableStyle.setColumnCenter(table, 8);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            BootstrapTableStyle.setHeaderRight(table, 5);
            BootstrapTableStyle.setHeaderRight(table, 6);
            BootstrapTableStyle.setHeaderLeft(table, 7);
            BootstrapTableStyle.setHeaderCenter(table, 8);

            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());

            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listProducts());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedLongId(table, model);
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (ProductRow r : allRows) {
                String hay = (r.sku() + " " + r.name() + " " + nv(r.categoryName()) + " " + nv(r.unitCode()) + " " + nv(r.taxCode()) + " " + (r.active() ? "active" : "inactive")).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{r.id(), r.sku(), r.name(), r.categoryName(), r.unitCode(), r.buyingPrice().setScale(2, RoundingMode.HALF_UP), r.sellingPrice().setScale(2, RoundingMode.HALF_UP), r.taxCode(), r.active() ? "Yes" : "No"});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (ProductRow r : allRows) {
                out.add(r.sku());
                out.add(r.name());
                out.add(r.categoryName());
            }
            return out.stream().filter(s -> s != null && !s.isBlank()).toList();
        }

        void onAdd() {
            ProductDialog d = new ProductDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createProduct(session.userId(), d.sku(), d.productName(), d.categoryId(), d.unitId(), d.buyingPrice(), d.sellingPrice(), d.taxId(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to create product.", (Exception) ex);
            }
        }

        void onEdit() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a product first.");
                return;
            }
            ProductRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            ProductDialog d = new ProductDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateProduct(session.userId(), id, d.sku(), d.productName(), d.categoryId(), d.unitId(), d.buyingPrice(), d.sellingPrice(), d.taxId(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to update product.", (Exception) ex);
            }
        }

        void onDelete() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a product first.");
                return;
            }
            if (!SwingUtils.confirm(ProductsPanel.this, "Delete selected product?")) {
                return;
            }
            try {
                service.deleteProduct(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to delete product.", (Exception) ex);
            }
        }

        final class ProductDialog extends JDialog {

            final JTextField txtSku = new JTextField(22);
            final JTextField txtName = new JTextField(22);
            final JComboBox<Object> cboCategory = new JComboBox<>();
            final JComboBox<LookupOption> cboUnit = new JComboBox<>();
            final JComboBox<Object> cboTax = new JComboBox<>();
            final JTextField txtBuying = new JTextField(22);
            final JTextField txtSelling = new JTextField(22);

            final JTextField txtSeniorDiscount = new JTextField(22);
            final JTextField txtTaxAmount = new JTextField(22);
            final JTextField txtNetProfitAmount = new JTextField(22);
            final JTextField txtProfitPercentage = new JTextField(22);

            final JCheckBox chkActive = new JCheckBox("Active", true);
            boolean saved;

            ProductDialog(ProductRow row) {
                super(SwingUtilities.getWindowAncestor(ProductsPanel.this),
                        row == null ? "Add Product" : "Edit Product",
                        ModalityType.APPLICATION_MODAL);

                chkActive.setOpaque(false);

                txtSeniorDiscount.setEditable(false);
                txtTaxAmount.setEditable(false);
                txtNetProfitAmount.setEditable(false);
                txtProfitPercentage.setEditable(false);

                cboCategory.addItem("(None)");
                for (LookupOption o : service.categoryOptions()) {
                    cboCategory.addItem(o);
                }

                for (LookupOption o : service.unitOptions()) {
                    cboUnit.addItem(o);
                }

                cboTax.addItem("(None)");
                for (LookupOption o : service.taxOptions()) {
                    cboTax.addItem(o);
                }

                applyComboBoxAutoFill(cboCategory);
                applyComboBoxAutoFill(cboUnit);
                applyComboBoxAutoFill(cboTax);

                if (row != null) {
                    txtSku.setText(row.sku());
                    txtName.setText(row.name());
                    txtBuying.setText(row.buyingPrice().toPlainString());
                    txtSelling.setText(row.sellingPrice().toPlainString());
                    chkActive.setSelected(row.active());
                    selectLookup(cboCategory, row.categoryId());
                    selectLookup(cboUnit, row.unitId());
                    selectLookup(cboTax, row.taxId());
                } else {
                    txtBuying.setText("0");
                    txtSelling.setText("0");
                }

                DocumentListener calcListener = new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        recalculate();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        recalculate();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        recalculate();
                    }
                };

                txtBuying.getDocument().addDocumentListener(calcListener);
                txtSelling.getDocument().addDocumentListener(calcListener);
                cboTax.addActionListener(e -> recalculate());

                txtSku.setFont(font14Plain);
                txtSku.putClientProperty("JTextField.placeholderText", "SKU");
                txtSku.setPreferredSize(new Dimension(250, 30));

                txtName.setFont(font14Plain);
                txtName.putClientProperty("JTextField.placeholderText", "Name");
                txtName.setPreferredSize(new Dimension(250, 30));

                cboCategory.setFont(font14Plain);
                cboCategory.setPreferredSize(new Dimension(250, 30));

                cboUnit.setFont(font14Plain);
                cboUnit.setPreferredSize(new Dimension(250, 30));

                cboTax.setFont(font14Plain);
                cboTax.setPreferredSize(new Dimension(250, 30));

                txtBuying.setFont(font14Plain);
                txtBuying.putClientProperty("JTextField.placeholderText", "Buying price");
                txtBuying.setPreferredSize(new Dimension(250, 30));

                txtSelling.setFont(font14Plain);
                txtSelling.putClientProperty("JTextField.placeholderText", "Selling price");
                txtSelling.setPreferredSize(new Dimension(250, 30));

                chkActive.setFont(font14Plain);
                chkActive.setPreferredSize(new Dimension(250, 30));

                JPanel form = simpleForm(
                        new String[]{
                            "SKU",
                            "Name",
                            "Category",
                            "Unit",
                            "Tax",
                            "Buying Price",
                            "Selling Price",
                            "Status"
                        },
                        new JComponent[]{
                            txtSku,
                            txtName,
                            cboCategory,
                            cboUnit,
                            cboTax,
                            txtBuying,
                            txtSelling,
                            chkActive
                        }
                );

                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setIcon(iconClose);
                cancel.setDanger();

                ok.addActionListener(e -> {
                    if (cboUnit.getSelectedItem() == null) {
                        SwingUtils.info(this, "Unit is required.");
                        return;
                    }
                    try {
                        buyingPrice();
                        sellingPrice();
                        recalculate();
                        saved = true;
                        dispose();
                    } catch (Exception ex) {
                        SwingUtils.info(this, "Buying and selling price must be numeric.");
                    }
                });

                cancel.addActionListener(e -> dispose());

                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);

                recalculate();

                pack();
                setLocationRelativeTo(ProductsPanel.this);
            }

            String sku() {
                return txtSku.getText().trim();
            }

            String productName() {
                return txtName.getText().trim();
            }

            Integer categoryId() {
                Object o = cboCategory.getSelectedItem();
                return o instanceof LookupOption lo ? lo.id() : null;
            }

            int unitId() {
                return ((LookupOption) cboUnit.getSelectedItem()).id();
            }

            Integer taxId() {
                Object o = cboTax.getSelectedItem();
                return o instanceof LookupOption lo ? lo.id() : null;
            }

            BigDecimal buyingPrice() {
                return parseDecimal(txtBuying.getText());
            }

            BigDecimal sellingPrice() {
                return parseDecimal(txtSelling.getText());
            }

            boolean active() {
                return chkActive.isSelected();
            }

            private BigDecimal parseDecimal(String s) {
                String v = s == null ? "" : s.trim();
                if (v.isBlank()) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(v);
            }

            private void recalculate() {
                BigDecimal buying = safeDecimal(txtBuying.getText());
                BigDecimal selling = safeDecimal(txtSelling.getText());

                BigDecimal seniorDiscount = selling.multiply(new BigDecimal("0.20"));
                BigDecimal taxAmount = selling.multiply(extractTaxRate());
                BigDecimal netProfitAmount = selling.subtract(taxAmount.add(seniorDiscount));

                BigDecimal profitPercentage = BigDecimal.ZERO;
                if (buying.compareTo(BigDecimal.ZERO) > 0) {
                    profitPercentage = netProfitAmount.subtract(buying)
                            .divide(buying, 6, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                }

                txtSeniorDiscount.setText(formatMoney(seniorDiscount));
                txtTaxAmount.setText(formatMoney(taxAmount));
                txtNetProfitAmount.setText(formatMoney(netProfitAmount));
                txtProfitPercentage.setText(profitPercentage.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%");
            }

            private BigDecimal safeDecimal(String s) {
                try {
                    return parseDecimal(s);
                } catch (Exception ex) {
                    return BigDecimal.ZERO;
                }
            }

            private String formatMoney(BigDecimal value) {
                return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
            }

            private BigDecimal extractTaxRate() {
                Object selected = cboTax.getSelectedItem();
                if (selected == null) {
                    return BigDecimal.ZERO;
                }

                String text = selected.toString();
                Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%").matcher(text);
                if (m.find()) {
                    return new BigDecimal(m.group(1))
                            .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                }

                return BigDecimal.ZERO;
            }
        }
    }

    private final class BarcodeTab extends BaseTab {

        final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Product", "Barcode", "Primary", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        final List<BarcodeRow> allRows = new ArrayList<>();

        BarcodeTab() {
            StyledButton btnAdd = new StyledButton("Add");
            StyledButton btnEdit = new StyledButton("Edit");
            StyledButton btnDelete = new StyledButton("Delete");
            StyledButton btnRefresh = new StyledButton("Refresh");
            btnAdd.setIcon(iconAdd);
            btnEdit.setIcon(iconEdit);
            btnDelete.setIcon(iconDelete);
            btnRefresh.setIcon(iconRefresh);
            btnAdd.setEnabled(canWrite);
            btnEdit.setEnabled(canWrite);
            btnDelete.setEnabled(canWrite);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            BootstrapTableStyle.install(table);

            int[] widths = {0, 300, 180, 100, 100};
            for (int i = 0; i < widths.length; i++) {
                BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
            }
            
            BootstrapTableStyle.hideColumns(table, 0);
            BootstrapTableStyle.setColumnLeft(table, 1);
            BootstrapTableStyle.setColumnLeft(table, 2);
            BootstrapTableStyle.setColumnLeft(table, 3);
            BootstrapTableStyle.setColumnLeft(table, 4);

            BootstrapTableStyle.setHeaderLeft(table, 1);
            BootstrapTableStyle.setHeaderLeft(table, 2);
            BootstrapTableStyle.setHeaderLeft(table, 3);
            BootstrapTableStyle.setHeaderLeft(table, 4);
            
            add(buildToolbar(txtSearch, spnLimit, btnAdd, btnEdit, btnRefresh), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> onEdit());
            btnDelete.addActionListener(e -> onDelete());
            btnRefresh.addActionListener(e -> refresh());
            installSuggest();
        }

        @Override
        void refresh() {
            allRows.clear();
            allRows.addAll(service.listBarcodes());
            applyFilter();
        }

        @Override
        void applyFilter() {
            Long keepId = selectedLongId(table, model);
            model.setRowCount(0);
            String q = query();
            int count = 0;
            for (BarcodeRow r : allRows) {
                String hay = (r.productName() + " " + r.barcode() + " " + (r.primary() ? "primary" : "") + " " + (r.active() ? "active" : "inactive")).toLowerCase();
                if (!match(hay, q)) {
                    continue;
                }
                model.addRow(new Object[]{r.id(), r.productName(), r.barcode(), r.primary() ? "Yes" : "No", r.active() ? "Yes" : "No"});
                if (++count >= limit()) {
                    break;
                }
            }
            reselect(table, model, keepId);
        }

        @Override
        List<String> suggestions() {
            Set<String> out = new LinkedHashSet<>();
            for (BarcodeRow r : allRows) {
                out.add(r.productName());
                out.add(r.barcode());
            }
            return new ArrayList<>(out);
        }

        void onAdd() {
            BarcodeDialog d = new BarcodeDialog(null);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.createBarcode(session.userId(), d.productId(), d.barcode(), d.primary(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to create barcode.", (Exception) ex);
            }
        }

        void onEdit() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a barcode first.");
                return;
            }
            BarcodeRow row = allRows.stream().filter(r -> r.id() == id).findFirst().orElse(null);
            if (row == null) {
                return;
            }
            BarcodeDialog d = new BarcodeDialog(row);
            d.setVisible(true);
            if (!d.saved) {
                return;
            }
            try {
                service.updateBarcode(session.userId(), id, d.productId(), d.barcode(), d.primary(), d.active());
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to update barcode.", (Exception) ex);
            }
        }

        void onDelete() {
            Long id = selectedLongId(table, model);
            if (id == null) {
                SwingUtils.info(ProductsPanel.this, "Select a barcode first.");
                return;
            }
            if (!SwingUtils.confirm(ProductsPanel.this, "Delete selected barcode?")) {
                return;
            }
            try {
                service.deleteBarcode(session.userId(), id);
                refreshAll();
            } catch (Exception ex) {
                SwingUtils.error(ProductsPanel.this, "Failed to delete barcode.", (Exception) ex);
            }
        }

        final class BarcodeDialog extends JDialog {

            final JComboBox<LookupOption> cboProduct = new JComboBox<>();
            final JTextField txtBarcode = new JTextField(22);
            final JCheckBox chkPrimary = new JCheckBox("Primary");
            final JCheckBox chkActive = new JCheckBox("Active", true);
            boolean saved;

            BarcodeDialog(BarcodeRow row) {
                super(SwingUtilities.getWindowAncestor(ProductsPanel.this), row == null ? "Add Barcode" : "Edit Barcode", ModalityType.APPLICATION_MODAL);
                chkPrimary.setOpaque(false);
                chkActive.setOpaque(false);
                for (LookupOption o : service.productOptions()) {
                    cboProduct.addItem(o);
                }

                applyComboBoxAutoFill(cboProduct);
                if (row != null) {
                    selectLookup(cboProduct, (int) row.id());
                    Object selected = cboProduct.getSelectedItem();
                    if (selected != null) {
                        JTextComponent editor = (JTextComponent) cboProduct.getEditor().getEditorComponent();
                        editor.setText(selected.toString());
                    }
                    txtBarcode.setText(row.barcode());
                    chkPrimary.setSelected(row.primary());
                    chkActive.setSelected(row.active());
                    selectLookup(cboProduct, (int) row.productId());
                }
                JPanel form = simpleForm(new String[]{"Product", "Barcode", "Primary", "Status"}, new JComponent[]{cboProduct, txtBarcode, chkPrimary, chkActive});
                StyledButton ok = new StyledButton("Save");
                ok.setIcon(iconSave);
                StyledButton cancel = new StyledButton("Cancel");
                cancel.setDanger();
                cancel.setIcon(iconClose);
                ok.addActionListener(e -> {
                    if (cboProduct.getSelectedItem() == null || barcode().isBlank()) {
                        SwingUtils.info(this, "Product and barcode are required.");
                        return;
                    }
                    saved = true;
                    dispose();
                });
                cancel.addActionListener(e -> dispose());
                setLayout(new BorderLayout());
                add(form, BorderLayout.CENTER);
                add(buttonBar(ok, cancel), BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(ProductsPanel.this);
            }

            long productId() {
                return ((LookupOption) cboProduct.getSelectedItem()).id();
            }

            String barcode() {
                return txtBarcode.getText().trim();
            }

            boolean primary() {
                return chkPrimary.isSelected();
            }

            boolean active() {
                return chkActive.isSelected();
            }
        }
    }

    private JPanel simpleForm(String[] labels, JComponent[] comps) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            //            form.add(new JLabel(labels[i]), g);
            JLabel label = new JLabel(labels[i]);
            label.setFont(font14Bold);
            form.add(label, g);
            g.gridx = 1;
            g.weightx = 1;
            form.add(comps[i], g);
        }
        return form;
    }

    private JPanel buttonBar(StyledButton ok, StyledButton cancel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.add(ok);
        p.add(cancel);
        return p;
    }

    private void reselect(JTable table, DefaultTableModel model, Object keepId) {
        if (model.getRowCount() == 0) {
            return;
        }
        int target = 0;
        if (keepId != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (keepId.equals(model.getValueAt(i, 0))) {
                    target = i;
                    break;
                }
            }
        }
        table.setRowSelectionInterval(target, target);
    }

    private Integer selectedIntId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return ((Number) model.getValueAt(table.convertRowIndexToModel(row), 0)).intValue();
    }

    private Long selectedLongId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return ((Number) model.getValueAt(table.convertRowIndexToModel(row), 0)).longValue();
    }

    private String nv(String s) {
        return s == null ? "" : s;
    }

    private void selectLookup(JComboBox<?> combo, Integer id) {
        if (id == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object o = combo.getItemAt(i);
            if (o instanceof LookupOption lo && lo.id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void centerHeader(JTable table, int columnIndex) {
        TableCellRenderer original = table.getTableHeader().getDefaultRenderer();

        table.getColumnModel().getColumn(columnIndex).setHeaderRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                Component c = original.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col
                );

                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });
    }

    private static void applyComboBoxAutoFill(JComboBox<?> combo) {
        combo.setEditable(true);
        JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();
        editor.setDocument(new ComboBoxAutoFill(combo));
    }

}
