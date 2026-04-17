package com.aldrin.ensarium.shift;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ShiftDao {

    public Shift findOpenShiftForUser(int userId, int terminalId) throws Exception {
        String sql = """
SELECT id, store_id, terminal_id, opened_by, opened_at, opening_cash, status,closed_at,closing_cash,opening_cash,status,remarks 
FROM pos_shift
WHERE opened_by = ?
  AND terminal_id = ?
  AND status = 'OPEN'
ORDER BY opened_at DESC
FETCH FIRST ROW ONLY
        """;

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, terminalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                java.sql.Timestamp openedAtTs = rs.getTimestamp("opened_at");
                java.sql.Timestamp closedAtTs = rs.getTimestamp("closed_at");
                return new Shift(
                        rs.getLong("id"),
                        rs.getInt("store_id"),
                        rs.getInt("terminal_id"),
                        rs.getInt("opened_by"),
                        openedAtTs == null ? null : openedAtTs.toLocalDateTime(),
                        null,
                        closedAtTs == null ? null : closedAtTs.toLocalDateTime(),
                        rs.getBigDecimal("opening_cash"),
                        rs.getBigDecimal("closing_cash"),
                        rs.getString("status"),
                        rs.getString("remarks")
                );
            }
        }
    }

    public Shift openShift(Session session, BigDecimal openingCash, String remarks) throws SQLException {
        if (session == null) {
            throw new SQLException("No active session.");
        }

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Only block the SAME USER from opening another shift on the same terminal.
                // Do NOT block other users just because someone else left a shift open.
                Shift existing = findOpenShift(con, session.getUserId(), session.getTerminalId());
                if (existing != null) {
                    throw new SQLException("This cashier already has an OPEN shift on the current terminal.");
                }

                String sql = """
                        INSERT INTO pos_shift (
                            store_id, terminal_id, opened_by, opened_at,
                            opening_cash, status, remarks
                        )
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, 'OPEN', ?)
                        """;

                long newId;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, session.getStoreId());
                    ps.setInt(2, session.getTerminalId());
                    ps.setInt(3, session.getUserId());
                    ps.setBigDecimal(4, openingCash);
                    ps.setString(5, remarks);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not obtain new shift id.");
                        }
                        newId = keys.getLong(1);
                    }
                }

                con.commit();
                return findById(newId);
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

//    public void closeShift(long shiftId, int userId, BigDecimal closingCash) throws SQLException {
//        String sql = """
//                UPDATE pos_shift
//                SET closed_by = ?,
//                    closed_at = CURRENT_TIMESTAMP,
//                    closing_cash = ?,
//                    status = 'CLOSED'
//                WHERE id = ?
//                  AND status = 'OPEN'
//                """;
//
//        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.setBigDecimal(2, closingCash);
//            ps.setLong(3, shiftId);
//            int updated = ps.executeUpdate();
//            if (updated == 0) {
//                throw new SQLException("Shift was not updated. It may already be closed.");
//            }
//        }
//    }
    public void closeShift(long shiftId, int userId, BigDecimal closingCash) throws Exception {
        String sql = """
        UPDATE pos_shift
        SET status = 'CLOSED',
            closed_at = CURRENT_TIMESTAMP,
            closing_cash = ?
        WHERE id = ?
          AND status = 'OPEN'
        """;

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, closingCash);
            ps.setLong(2, shiftId);
            ps.executeUpdate();
        }
    }

    public Shift findById(long shiftId) throws SQLException {
        String sql = """
                SELECT id, store_id, terminal_id, opened_by, opened_at, closed_by, closed_at,
                       opening_cash, closing_cash, status, remarks
                FROM pos_shift
                WHERE id = ?
                """;

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Shift not found: " + shiftId);
                }
                return mapShift(rs);
            }
        }
    }

    private Shift findOpenShift(Connection con, int userId, int terminalId) throws SQLException {
        String sql = """
                SELECT id, store_id, terminal_id, opened_by, opened_at, closed_by, closed_at,
                       opening_cash, closing_cash, status, remarks
                FROM pos_shift
                WHERE opened_by = ?
                  AND terminal_id = ?
                  AND status = 'OPEN'
                FETCH FIRST ROW ONLY
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, terminalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapShift(rs) : null;
            }
        }
    }

    private Shift mapShift(ResultSet rs) throws SQLException {
        return new Shift(
                rs.getLong("id"),
                rs.getInt("store_id"),
                rs.getInt("terminal_id"),
                rs.getInt("opened_by"),
                toLocalDateTime(rs.getTimestamp("opened_at")),
                getNullableInt(rs, "closed_by"),
                toLocalDateTime(rs.getTimestamp("closed_at")),
                rs.getBigDecimal("opening_cash"),
                rs.getBigDecimal("closing_cash"),
                rs.getString("status"),
                rs.getString("remarks")
        );
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
