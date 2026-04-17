package com.aldrin.ensarium.dispense;

import com.aldrin.ensarium.db.AppConfig;
import com.aldrin.ensarium.db.Db;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class HotkeyService {
    public static final String ACT_FOCUS_BARCODE = "FOCUS_BARCODE";
    public static final String ACT_SEARCH = "SEARCH";
    public static final String ACT_CUSTOMER = "CUSTOMER";
    public static final String ACT_DISCOUNT = "DISCOUNT";
    public static final String ACT_HOLD = "HOLD";
    public static final String ACT_LOAD_HOLD = "LOAD_HOLD";
    public static final String ACT_PAY = "PAY";
    public static final String ACT_RETURN = "RETURN";
    public static final String ACT_HISTORY = "HISTORY";
    public static final String ACT_NEW_SALE = "NEW_SALE";
    public static final String ACT_HOTKEYS = "HOTKEYS";

    private final int userId = AppConfig.getInt("app.userId", 1);

    public Map<String, KeyStroke> loadEffectiveHotkeys() throws Exception {
        Map<String, KeyStroke> map = defaultHotkeys();
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT action_code, key_stroke, enabled FROM user_hotkey WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getInt("enabled") == 1) {
                            KeyStroke ks = KeyStroke.getKeyStroke(rs.getString("key_stroke"));
                            if (ks != null) map.put(rs.getString("action_code"), ks);
                        }
                    }
                }
            }
            conn.commit();
        }
        return map;
    }

    public void saveHotkey(String actionCode, KeyStroke keyStroke, boolean enabled) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            int updated;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user_hotkey SET key_stroke = ?, enabled = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND action_code = ?")) {
                ps.setString(1, keyStroke == null ? null : keyStroke.toString());
                ps.setInt(2, enabled ? 1 : 0);
                ps.setInt(3, userId);
                ps.setString(4, actionCode);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO user_hotkey(user_id, action_code, key_stroke, enabled, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, actionCode);
                    ps.setString(3, keyStroke == null ? null : keyStroke.toString());
                    ps.setInt(4, enabled ? 1 : 0);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        }
    }

    public static Map<String, String> actionLabels() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(ACT_FOCUS_BARCODE, "Focus Barcode");
        m.put(ACT_SEARCH, "Search Product");
        m.put(ACT_CUSTOMER, "Select Customer");
        m.put(ACT_DISCOUNT, "Discount");
        m.put(ACT_HOLD, "Hold Ticket");
        m.put(ACT_LOAD_HOLD, "Unhold Ticket");
        m.put(ACT_PAY, "Pay");
        m.put(ACT_RETURN, "Return");
        m.put(ACT_HISTORY, "Sale History");
        m.put(ACT_NEW_SALE, "New Sale");
        m.put(ACT_HOTKEYS, "Hotkey Settings");
        return m;
    }

    public static Map<String, KeyStroke> defaultHotkeys() {
        Map<String, KeyStroke> m = new LinkedHashMap<>();
        m.put(ACT_FOCUS_BARCODE, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        m.put(ACT_SEARCH, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        m.put(ACT_CUSTOMER, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        m.put(ACT_DISCOUNT, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        m.put(ACT_HOLD, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        m.put(ACT_LOAD_HOLD, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        m.put(ACT_PAY, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        m.put(ACT_RETURN, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        m.put(ACT_HISTORY, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        m.put(ACT_NEW_SALE, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        m.put(ACT_HOTKEYS, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        return m;
    }

    public static void bind(JComponent root, Map<String, KeyStroke> keys, Map<String, Action> actions) {
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        for (Map.Entry<String, KeyStroke> e : defaultHotkeys().entrySet()) {
            if (e.getValue() != null) im.remove(e.getValue());
            am.remove(e.getKey());
        }
        for (Map.Entry<String, KeyStroke> e : keys.entrySet()) {
            if (e.getValue() == null) continue;
            im.put(e.getValue(), e.getKey());
            am.put(e.getKey(), actions.get(e.getKey()));
        }
    }

    public static String toHuman(KeyStroke ks) {
        if (ks == null) return "-";
        String s = ks.toString().replace("pressed ", "").replace("ctrl ", "Ctrl+").replace("shift ", "Shift+").replace("alt ", "Alt+");
        s = s.replace("meta ", "Meta+");
        return s.toUpperCase().replace("+ ", "+");
    }
}
