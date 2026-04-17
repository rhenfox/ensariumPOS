package com.aldrin.ensarium.ui.panels;

import com.aldrin.ensarium.icons.FaSwingIcons;
import com.aldrin.ensarium.model.AuditRow;
import com.aldrin.ensarium.security.Session;
import com.aldrin.ensarium.service.AuditService;
import com.aldrin.ensarium.ui.widgets.BootstrapTableStyle;
import com.aldrin.ensarium.util.AutoSuggestSupport;
import com.aldrin.ensarium.ui.widgets.StyledButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AuditLogPanel extends JPanel {

    private static final SimpleDateFormat CREATED_AT_FORMAT = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    private final AuditService auditService = new AuditService();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Username", "Full name", "Action", "Details", "Created at"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(100, 1, 10_000, 1));
    private final List<AuditRow> allAudit = new ArrayList<>();

    Icon iconRefresh = FaSwingIcons.icon(FontAwesomeIcon.REFRESH, 24, Color.WHITE);

    private final Font font14Plain = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font font14Bold = new Font("Segoe UI", Font.BOLD, 14);

    public AuditLogPanel(Session session) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        txtSearch.putClientProperty("JTextField.placeholderText", "Search...");

        JLabel title = new JLabel("Audit Log");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        StyledButton btnRefresh = new StyledButton("Refresh");
        btnRefresh.setIcon(iconRefresh);
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel searchl = new JLabel("Search");
        searchl.setFont(font14Bold);
        leftTools.add(searchl);
        txtSearch.setPreferredSize(new Dimension(250, 30));
        txtSearch.setFont(font14Plain);
        leftTools.add(txtSearch);
        JLabel limitl = new JLabel("Limit");
        limitl.setFont(font14Bold);
        limitl.setFont(font14Bold);
        leftTools.add(limitl);
        ((JSpinner.DefaultEditor) spnLimit.getEditor()).getTextField().setColumns(5);
        spnLimit.setPreferredSize(new Dimension(80, 30));
        leftTools.add(spnLimit);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.add(btnRefresh);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(leftTools, BorderLayout.WEST);
        top.add(rightTools, BorderLayout.EAST);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        BootstrapTableStyle.install(table);

        int[] widths = {55, 150, 200, 250, 300, 200};
        for (int i = 0; i < widths.length; i++) {
            BootstrapTableStyle.setColumnWidth(table, i, widths[i]);
        }
        BootstrapTableStyle.hideColumns(table, 0);
        BootstrapTableStyle.setColumnLeft(table, 1);
        BootstrapTableStyle.setColumnLeft(table, 2);
        BootstrapTableStyle.setColumnLeft(table, 3);
        BootstrapTableStyle.setColumnLeft(table, 4);
        BootstrapTableStyle.setColumnLeft(table, 5);
        
        BootstrapTableStyle.setHeaderLeft(table, 1);
        BootstrapTableStyle.setHeaderLeft(table, 2);
        BootstrapTableStyle.setHeaderLeft(table, 3);
        BootstrapTableStyle.setHeaderLeft(table, 4);
        BootstrapTableStyle.setHeaderLeft(table, 5);
        for (int i = 6; i <= 13; i++) {
            BootstrapTableStyle.setColumnRight(table, i);
        }


        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refresh());
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
        AutoSuggestSupport.install(txtSearch, this::auditSuggestions);

        refresh();
    }

    public void refresh() {
        allAudit.clear();
        allAudit.addAll(auditService.listAudit());
        applyFilter();
    }

    private void applyFilter() {
        model.setRowCount(0);
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        int limit = (Integer) spnLimit.getValue();
        int count = 0;

        for (AuditRow row : allAudit) {
            String hay = (nv(row.actorUsername()) + " " + nv(row.actorFullName()) + " " + nv(row.actionCode()) + " " + nv(row.details()) + " " + String.valueOf(row.createdAt())).toLowerCase();
            if (!q.isEmpty() && !hay.contains(q)) {
                continue;
            }
            model.addRow(new Object[]{row.id(), row.actorUsername(), row.actorFullName(), row.actionCode(), row.details(), new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a").format(row.createdAt())});
            count++;
            if (count >= limit) {
                break;
            }
        }
    }


    private List<String> auditSuggestions() {
        Set<String> out = new LinkedHashSet<>();
        for (AuditRow row : allAudit) {
            out.add(row.actorUsername());
            out.add(row.actorFullName());
            out.add(row.actionCode());
            out.add(row.details());
        }
        return new ArrayList<>(out);
    }

    private String nv(String s) {
        return s == null ? "" : s;
    }
}
