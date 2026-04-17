package com.aldrin.ensarium.shift;

import com.aldrin.ensarium.db.Db;
import com.aldrin.ensarium.security.Session;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SalesDao {

    private static final DateTimeFormatter SALE_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

//    public List<MethodTotal> totalsPerPaymentMethod(long shiftId, int cashierUserId) throws SQLException {
//        String sql = """
//                SELECT 
//                    APP.PAYMENT_METHOD.ID, 
//                    APP.POS_SHIFT.OPENED_BY, 
//                    SUM(APP.SALE_LINE.QTY_IN_BASE*APP.SALE_LINE.UNIT_PRICE) AS AMOUNT, 
//                    APP.PAYMENT_METHOD.CODE, 
//                    APP.PAYMENT_METHOD.NAME
//                FROM APP.SALE
//                INNER JOIN APP.POS_SHIFT 
//                    ON APP.SALE.SHIFT_ID = APP.POS_SHIFT.ID
//                INNER JOIN APP.SALE_LINE 
//                    ON APP.SALE.ID = APP.SALE_LINE.SALE_ID
//                INNER JOIN APP.SALE_PAYMENT 
//                    ON APP.SALE.ID = APP.SALE_PAYMENT.SALE_ID
//                INNER JOIN APP.PAYMENT_METHOD 
//                    ON APP.SALE_PAYMENT.METHOD_ID = APP.PAYMENT_METHOD.ID
//                WHERE APP.POS_SHIFT.STATUS = 'OPEN'
//                  AND APP.POS_SHIFT.OPENED_BY = ?
//                GROUP BY 
//                    APP.PAYMENT_METHOD.ID, 
//                    APP.POS_SHIFT.OPENED_BY, 
//                    APP.PAYMENT_METHOD.CODE, 
//                    APP.PAYMENT_METHOD.NAME
//                """;
//
//        List<MethodTotal> result = new ArrayList<>();
//        try (Connection con = Db.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
    ////            ps.setLong(1, shiftId);
//            ps.setInt(1, cashierUserId);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    result.add(new MethodTotal(
//                            rs.getInt("ID"),
//                            rs.getString("CODE"),
//                            rs.getString("NAME"),
//                            rs.getBigDecimal("AMOUNT")
//                    ));
//                }
//            }
//        }
//        return result;
//    }
    
    
    public List<MethodTotal> totalsPerPaymentMethod(long shiftId, int cashierUserId) throws SQLException {
        String sql = """
SELECT 
    APP.PAYMENT_METHOD.ID, 
    APP.PAYMENT_METHOD.CODE, 
    APP.PAYMENT_METHOD.NAME, 
    COALESCE(SUM(APP.SALE_LINE.QTY_IN_BASE * APP.SALE_LINE.UNIT_PRICE),0) AS AMOUNT 
FROM 
    APP.SALE_PAYMENT 
INNER JOIN 
    APP.SALE 
ON ( APP.SALE_PAYMENT.SALE_ID = APP.SALE.ID) 
INNER JOIN 
    APP.PAYMENT_METHOD 
ON (APP.SALE_PAYMENT.METHOD_ID = APP.PAYMENT_METHOD.ID) 
INNER JOIN 
    APP.SALE_LINE 
ON (APP.SALE.ID = APP.SALE_LINE.SALE_ID)
            WHERE SALE.SHIFT_ID = ?
              AND SALE.CASHIER_USER_ID = ?
              AND SALE.STATUS = 'POSTED'
            GROUP BY  APP.PAYMENT_METHOD.ID, 
    APP.PAYMENT_METHOD.CODE, 
    APP.PAYMENT_METHOD.NAME
            ORDER BY PAYMENT_METHOD.NAME
            """;

        List<MethodTotal> result = new ArrayList<>();

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shiftId);
            ps.setInt(2, cashierUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new MethodTotal(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getBigDecimal("amount")
                    ));
                }
            }
        }

        return result;
    }

    public List<PaymentDetail> paymentDetails(int cashierUserId, int paymentMethodId, long shiftId) throws SQLException {
        String sql = """
                SELECT 
                    S.SALE_NO, 
                    S.SOLD_AT, 
                    SUM(QTY_IN_BASE*SL.UNIT_PRICE) AS AMOUNT, 
                    SP.REFERENCE_NO, 
                    S.NOTES
                FROM APP.SALE S
                INNER JOIN APP.SALE_LINE SL
                    ON S.ID = SL.SALE_ID
                INNER JOIN APP.SALE_PAYMENT SP
                    ON S.ID = SP.SALE_ID
                WHERE S.CASHIER_USER_ID = ?
                  AND SP.METHOD_ID = ?
                  AND S.SHIFT_ID = ?
                GROUP BY 
                    S.SALE_NO, 
                    S.SOLD_AT, 
                    SP.REFERENCE_NO, 
                    S.NOTES  ORDER BY S.SALE_NO DESC 
                """;

        List<PaymentDetail> result = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cashierUserId);
            ps.setInt(2, paymentMethodId);
            ps.setLong(3, shiftId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PaymentDetail(
                            rs.getString("sale_no"),
                            rs.getTimestamp("sold_at").toLocalDateTime(),
                            rs.getBigDecimal("amount"),
                            rs.getString("reference_no"),
                            rs.getString("notes")
                    ));
                }
            }
        }
        return result;
    }

    public int countSales(long shiftId, int cashierUserId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM sale
                WHERE shift_id = ?
                  AND cashier_user_id = ?
                  AND status = 'POSTED'
                """;
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shiftId);
            ps.setInt(2, cashierUserId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    
    
    public BigDecimal grandTotalPayments(long shiftId, int cashierUserId) throws SQLException {
        String sql = """
SELECT 
    COALESCE(SUM(APP.SALE_LINE.QTY_IN_BASE * APP.SALE_LINE.UNIT_PRICE),0) AS AMOUNT 
FROM 
    APP.SALE_PAYMENT 
INNER JOIN 
    APP.SALE 
ON (APP.SALE_PAYMENT.SALE_ID = APP.SALE.ID) 
INNER JOIN 
    APP.PAYMENT_METHOD 
ON  (APP.SALE_PAYMENT.METHOD_ID = APP.PAYMENT_METHOD.ID) 
INNER JOIN 
    APP.SALE_LINE 
ON (APP.SALE.ID = APP.SALE_LINE.SALE_ID)
            WHERE SALE.SHIFT_ID = ?
              AND SALE.CASHIER_USER_ID = ?
              AND SALE.STATUS = 'POSTED'
            """;

        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shiftId);
            ps.setInt(2, cashierUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal("AMOUNT");
                    return amount != null ? amount : BigDecimal.ZERO;
                }
                return BigDecimal.ZERO;
            }
        }
    }

    public List<PaymentMethod> findActivePaymentMethods() throws SQLException {
        String sql = """
                SELECT id, code, name
                FROM payment_method
                WHERE active = 1
                ORDER BY name
                """;
        List<PaymentMethod> list = new ArrayList<>();
        try (Connection con = Db.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PaymentMethod(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name")
                ));
            }
        }
        return list;
    }

    public long recordSimpleSale(Session session, long shiftId, int paymentMethodId, BigDecimal amount, String note) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Sale amount must be greater than zero.");
        }

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                long saleId = insertSale(con, session, shiftId, note);
                insertPayment(con, saleId, paymentMethodId, amount);
                con.commit();
                return saleId;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private long insertSale(Connection con, Session session, long shiftId, String note) throws SQLException {
        String sql = """
                INSERT INTO sale (
                    store_id, terminal_id, shift_id, sale_no, sale_type, status,
                    cashier_user_id, sold_at, notes
                )
                VALUES (?, ?, ?, ?, 'RETAIL', 'POSTED', ?, CURRENT_TIMESTAMP, ?)
                """;

        String saleNo = "S-" + session.getTerminalCode() + "-" + LocalDateTime.now().format(SALE_NO_FORMAT);

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getStoreId());
            ps.setInt(2, session.getTerminalId());
            ps.setLong(3, shiftId);
            ps.setString(4, saleNo);
            ps.setInt(5, session.userId());
            ps.setString(6, note);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not obtain sale id.");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertPayment(Connection con, long saleId, int paymentMethodId, BigDecimal amount) throws SQLException {
        String sql = """
                INSERT INTO sale_payment (sale_id, method_id, amount, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            ps.setInt(2, paymentMethodId);
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();
        }
    }

    public record MethodTotal(int methodId, String methodCode, String methodName, BigDecimal amount) {

    }

    public record PaymentMethod(int id, String code, String name) {

        @Override
        public String toString() {
            return code + " - " + name;
        }
    }

    public record PaymentDetail(String saleNo, LocalDateTime soldAt, BigDecimal amount, String referenceNo, String notes) {

    }
}
