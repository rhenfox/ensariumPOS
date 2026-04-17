package com.aldrin.ensarium.txn;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class TxnModuleMain {
    private TxnModuleMain() {}

    public static void main(String[] args) throws Exception {
        boolean derbyReady = initializeDatabase();

        if (GraphicsEnvironment.isHeadless()) {
            InventoryTxnDao dao = new InventoryTxnDao();
            List<InventoryTxn> rows = dao.findTransactions("", 100);
            requireType(rows, "PURCHASE_RECEIPT");
            requireType(rows, "SALE");
            requireType(rows, "SALE_RETURN");
            requireType(rows, "DAMAGE");
            requireType(rows, "EXPIRE");
            requireType(rows, "ADJUSTMENT");
            for (InventoryTxn txn : rows) {
                dao.findDetailsByTxnId(txn.getId());
                dao.findSummaryRows(txn);
                dao.findTraceRows(txn);
            }
            System.out.println((derbyReady ? "HEADLESS DERBY SMOKE TEST PASSED" : "HEADLESS FALLBACK SMOKE TEST PASSED"));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ensarium Transaction Module Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
//            frame.add(new TxnPanel(new Session(1, "admin", "Administrator")), BorderLayout.CENTER);
            frame.setSize(1280, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static boolean initializeDatabase() {
        try {
//            Db.startServerAndInit();
            return true;
        } catch (Throwable ex) {
            TxnDemoDatabase.installSampleData();
            return false;
        }
    }

    private static void requireType(List<InventoryTxn> rows, String txnType) {
        boolean found = rows.stream().anyMatch(r -> txnType.equalsIgnoreCase(r.getTxnType()));
        if (!found) {
            throw new IllegalStateException("Missing transaction type in demo data: " + txnType);
        }
    }
}
