package com.aldrin.ensarium.service;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.model.LookupOption;
import com.aldrin.ensarium.model.ShiftRow;
import com.aldrin.ensarium.model.StoreRow;
import com.aldrin.ensarium.model.TerminalRow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreAdminService {

    private final AuditService auditService = new AuditService();

    public List<StoreRow> listStores() {
        String sql = "SELECT id, code, name, address, active FROM store ORDER BY code";
        List<StoreRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new StoreRow(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load stores", ex);
        }
    }

    public int createStore(Integer actorUserId, String code, String name, String address, boolean active) {
        String sql = "INSERT INTO store(code, name, address, active) VALUES(?, ?, ?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trim(code));
            ps.setString(2, trim(name));
            ps.setString(3, nullable(address));
            ps.setInt(4, active ? 1 : 0);
            ps.executeUpdate();
            int id = generatedId(ps);
            auditService.log(actorUserId, "STORE_CREATE", "Created store: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create store", ex);
        }
    }

    public void updateStore(Integer actorUserId, int id, String code, String name, String address, boolean active) {
        String sql = "UPDATE store SET code=?, name=?, address=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trim(code));
            ps.setString(2, trim(name));
            ps.setString(3, nullable(address));
            ps.setInt(4, active ? 1 : 0);
            ps.setInt(5, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "STORE_UPDATE", "Updated store id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update store", ex);
        }
    }

    public void deleteStore(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM store WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "STORE_DELETE", "Deleted store id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete store", ex);
        }
    }

    public List<TerminalRow> listTerminals() {
        String sql = """
                SELECT t.id, t.store_id, s.code AS store_code, s.name AS store_name,
                       t.code, t.name, t.active
                FROM terminal t
                JOIN store s ON s.id = t.store_id
                ORDER BY s.code, t.code
                """;
        List<TerminalRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new TerminalRow(
                        rs.getInt("id"),
                        rs.getInt("store_id"),
                        rs.getString("store_code"),
                        rs.getString("store_name"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getInt("active") == 1
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load terminals", ex);
        }
    }

    public int createTerminal(Integer actorUserId, int storeId, String code, String name, boolean active) {
        String sql = "INSERT INTO terminal(store_id, code, name, active) VALUES(?, ?, ?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, storeId);
            ps.setString(2, trim(code));
            ps.setString(3, nullable(name));
            ps.setInt(4, active ? 1 : 0);
            ps.executeUpdate();
            int id = generatedId(ps);
            auditService.log(actorUserId, "TERMINAL_CREATE", "Created terminal: " + code);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create terminal", ex);
        }
    }

    public void updateTerminal(Integer actorUserId, int id, int storeId, String code, String name, boolean active) {
        String sql = "UPDATE terminal SET store_id=?, code=?, name=?, active=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.setString(2, trim(code));
            ps.setString(3, nullable(name));
            ps.setInt(4, active ? 1 : 0);
            ps.setInt(5, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TERMINAL_UPDATE", "Updated terminal id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update terminal", ex);
        }
    }

    public void deleteTerminal(Integer actorUserId, int id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM terminal WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "TERMINAL_DELETE", "Deleted terminal id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete terminal", ex);
        }
    }

    public List<ShiftRow> listShifts() {
        String sql = """
                SELECT ps.id,
                       ps.store_id,
                       s.name AS store_name,
                       ps.terminal_id,
                       t.code AS terminal_code,
                       ps.opened_by,
                       uo.full_name AS opened_by_name,
                       ps.opened_at,
                       ps.closed_by,
                       uc.full_name AS closed_by_name,
                       ps.closed_at,
                       ps.opening_cash,
                       ps.closing_cash,
                       ps.status,
                       ps.remarks
                FROM pos_shift ps
                JOIN store s ON s.id = ps.store_id
                JOIN terminal t ON t.id = ps.terminal_id
                JOIN users uo ON uo.id = ps.opened_by
                LEFT JOIN users uc ON uc.id = ps.closed_by
                ORDER BY ps.opened_at DESC, ps.id DESC
                """;
        List<ShiftRow> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new ShiftRow(
                        rs.getLong("id"),
                        rs.getInt("store_id"),
                        rs.getString("store_name"),
                        rs.getInt("terminal_id"),
                        rs.getString("terminal_code"),
                        (Integer) rs.getObject("opened_by"),
                        displayName(rs.getString("opened_by_name")),
                        rs.getTimestamp("opened_at"),
                        (Integer) rs.getObject("closed_by"),
                        displayName(rs.getString("closed_by_name")),
                        rs.getTimestamp("closed_at"),
                        nvl(rs.getBigDecimal("opening_cash")),
                        rs.getBigDecimal("closing_cash"),
                        rs.getString("status"),
                        rs.getString("remarks")
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load shifts", ex);
        }
    }

    public long createShift(Integer actorUserId,
                            int storeId,
                            int terminalId,
                            int openedBy,
                            Timestamp openedAt,
                            Integer closedBy,
                            Timestamp closedAt,
                            BigDecimal openingCash,
                            BigDecimal closingCash,
                            String status,
                            String remarks) {
        String sql = """
                INSERT INTO pos_shift(
                    store_id, terminal_id, opened_by, opened_at,
                    closed_by, closed_at, opening_cash, closing_cash,
                    status, remarks
                ) VALUES(?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindShift(ps, storeId, terminalId, openedBy, openedAt, closedBy, closedAt, openingCash, closingCash, status, remarks);
            ps.executeUpdate();
            long id = generatedLongId(ps);
            auditService.log(actorUserId, "SHIFT_CREATE", "Created shift id=" + id);
            return id;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create shift", ex);
        }
    }

    public void updateShift(Integer actorUserId,
                            long id,
                            int storeId,
                            int terminalId,
                            int openedBy,
                            Timestamp openedAt,
                            Integer closedBy,
                            Timestamp closedAt,
                            BigDecimal openingCash,
                            BigDecimal closingCash,
                            String status,
                            String remarks) {
        String sql = """
                UPDATE pos_shift
                   SET store_id=?, terminal_id=?, opened_by=?, opened_at=?,
                       closed_by=?, closed_at=?, opening_cash=?, closing_cash=?,
                       status=?, remarks=?
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindShift(ps, storeId, terminalId, openedBy, openedAt, closedBy, closedAt, openingCash, closingCash, status, remarks);
            ps.setLong(11, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_UPDATE", "Updated shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update shift", ex);
        }
    }

    public void openShift(Integer actorUserId,
                          long id,
                          int openedBy,
                          Timestamp openedAt,
                          BigDecimal openingCash) {
        String sql = """
                UPDATE pos_shift
                   SET opened_by=?,
                       opened_at=?,
                       opening_cash=?,
                       status='OPEN',
                       closed_by=NULL,
                       closed_at=NULL,
                       closing_cash=NULL
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, openedBy);
            ps.setTimestamp(2, openedAt != null ? openedAt : new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(3, nvl(openingCash));
            ps.setLong(4, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_OPEN", "Opened shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to open shift", ex);
        }
    }

    public void closeShift(Integer actorUserId,
                           long id,
                           int closedBy,
                           Timestamp closedAt,
                           BigDecimal closingCash) {
        String sql = """
                UPDATE pos_shift
                   SET closed_by=?,
                       closed_at=?,
                       closing_cash=?,
                       status='CLOSED'
                 WHERE id=?
                """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, closedBy);
            ps.setTimestamp(2, closedAt != null ? closedAt : new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(3, nvl(closingCash));
            ps.setLong(4, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_CLOSE", "Closed shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to close shift", ex);
        }
    }

    public void updateShiftOpeningCash(Integer actorUserId, long id, BigDecimal openingCash) {
        String sql = "UPDATE pos_shift SET opening_cash=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nvl(openingCash));
            ps.setLong(2, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_OPENING_CASH", "Updated opening cash for shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update opening cash", ex);
        }
    }

    public void updateShiftClosingCash(Integer actorUserId, long id, BigDecimal closingCash) {
        String sql = "UPDATE pos_shift SET closing_cash=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nvl(closingCash));
            ps.setLong(2, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_CLOSING_CASH", "Updated closing cash for shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update closing cash", ex);
        }
    }

    public void updateShiftOpenedBy(Integer actorUserId, long id, int openedBy) {
        String sql = "UPDATE pos_shift SET opened_by=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, openedBy);
            ps.setLong(2, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_OPENED_BY", "Updated opened_by for shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update opened by", ex);
        }
    }

    public void updateShiftClosedBy(Integer actorUserId, long id, Integer closedBy) {
        String sql = "UPDATE pos_shift SET closed_by=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (closedBy == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, closedBy);
            }
            ps.setLong(2, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_CLOSED_BY", "Updated closed_by for shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to update closed by", ex);
        }
    }

    public void deleteShift(Integer actorUserId, long id) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM pos_shift WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            auditService.log(actorUserId, "SHIFT_DELETE", "Deleted shift id=" + id);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to delete shift", ex);
        }
    }

    public List<LookupOption> storeOptions() {
        String sql = "SELECT id, code || ' - ' || name AS label FROM store ORDER BY code";
        return lookupList(sql, null);
    }

    public List<LookupOption> terminalOptions(Integer storeId) {
        String sql = storeId == null
                ? "SELECT id, code || COALESCE(' - ' || name, '') AS label FROM terminal ORDER BY code"
                : "SELECT id, code || COALESCE(' - ' || name, '') AS label FROM terminal WHERE store_id = ? ORDER BY code";
        return lookupList(sql, storeId);
    }

    public List<LookupOption> userOptions() {
        String sql = "SELECT id, username || COALESCE(' - ' || full_name, '') AS label FROM users WHERE active = 1 ORDER BY username";
        return lookupList(sql, null);
    }

    private List<LookupOption> lookupList(String sql, Integer id) {
        List<LookupOption> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (id != null) {
                ps.setInt(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new LookupOption(rs.getInt("id"), rs.getString("label")));
                }
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load lookup options", ex);
        }
    }

    private void bindShift(PreparedStatement ps,
                           int storeId,
                           int terminalId,
                           int openedBy,
                           Timestamp openedAt,
                           Integer closedBy,
                           Timestamp closedAt,
                           BigDecimal openingCash,
                           BigDecimal closingCash,
                           String status,
                           String remarks) throws SQLException {
        ps.setInt(1, storeId);
        ps.setInt(2, terminalId);
        ps.setInt(3, openedBy);
        ps.setTimestamp(4, openedAt != null ? openedAt : new Timestamp(System.currentTimeMillis()));
        if (closedBy == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, closedBy);
        if (closedAt == null) ps.setNull(6, Types.TIMESTAMP); else ps.setTimestamp(6, closedAt);
        ps.setBigDecimal(7, nvl(openingCash));
        if (closingCash == null) ps.setNull(8, Types.DECIMAL); else ps.setBigDecimal(8, closingCash);
        ps.setString(9, trim(status));
        ps.setString(10, nullable(remarks));
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
    private static String nullable(String value) {
        String v = value == null ? null : value.trim();
        return (v == null || v.isEmpty()) ? null : v;
    }
    private static int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No generated key returned.");
    }
    private static long generatedLongId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
        }
        throw new SQLException("No generated key returned.");
    }
    private static BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static String displayName(String name) { return name == null ? "" : name; }
}
