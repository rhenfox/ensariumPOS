package com.aldrin.ensarium.dashboard;


import com.aldrin.ensarium.db.Db;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {

    public List<StoreOption> loadStores() {
        List<StoreOption> stores = new ArrayList<>();
        stores.add(new StoreOption(null, "ALL", "All Stores"));
        String sql = "select id, code, name from store where active = 1 order by name";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stores.add(new StoreOption(rs.getInt("id"), rs.getString("code"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load stores", ex);
        }
        return stores;
    }

    public DashboardData loadDashboardData(Integer storeId, int days, Integer userId) {
        try (Connection con = Db.getConnection()) {
            BigDecimal todaySales = getTodaySales(con, storeId, userId);
            int todayTransactions = getTodayTransactions(con, storeId, userId);
            BigDecimal avgTicket = todayTransactions == 0
                    ? BigDecimal.ZERO
                    : todaySales.divide(BigDecimal.valueOf(todayTransactions), 2, RoundingMode.HALF_UP);
            BigDecimal todayProfit = getTodayProfit(con, storeId, userId);
            int openShifts = getOpenShifts(con, storeId, userId);
            int lowStockCount = getLowStockCount(con, storeId);
            List<TrendPoint> salesTrend = getSalesTrend(con, storeId, days, userId);
            List<CategoryAmount> topProducts = getTopProducts(con, storeId, days, userId);
            List<CategoryAmount> paymentMix = getPaymentMix(con, storeId, days, userId);
            List<CategoryAmount> inventoryMix = getInventoryMix(con, storeId);
            List<CategoryAmount> hourlySales = getHourlySales(con, storeId, userId);
            List<CategoryAmount> monthlyCategoryQtySales = getMonthlyCategoryQtySales(con, storeId, userId);
            List<LowStockItem> lowStockItems = getLowStockItems(con, storeId);
            return new DashboardData(todaySales, todayTransactions, avgTicket, todayProfit, openShifts, lowStockCount,
                    salesTrend, topProducts, paymentMix, inventoryMix, hourlySales, monthlyCategoryQtySales,
                    lowStockItems);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load dashboard data", ex);
        }
    }

    private BigDecimal getTodaySales(Connection con, Integer storeId, Integer userId) throws SQLException {
        String sql = "select coalesce(sum(sl.qty_in_base * sl.unit_price),0) "
                + "from sale s join sale_line sl on sl.sale_id = s.id "
                + "where s.status='POSTED' and date(s.sold_at)=current_date"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId);
        return queryBigDecimal(con, sql, storeId, userId);
    }

    private int getTodayTransactions(Connection con, Integer storeId, Integer userId) throws SQLException {
        String sql = "select count(*) from sale s where s.status='POSTED' and date(s.sold_at)=current_date"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId);
        return queryInt(con, sql, storeId, userId);
    }

    private BigDecimal getTodayProfit(Connection con, Integer storeId, Integer userId) throws SQLException {
        String sql = "select coalesce(sum((sl.qty_in_base * sl.unit_price) - sl.cost_total),0) "
                + "from sale s join sale_line sl on sl.sale_id = s.id "
                + "where s.status='POSTED' and date(s.sold_at)=current_date"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId);
        return queryBigDecimal(con, sql, storeId, userId);
    }

    private int getOpenShifts(Connection con, Integer storeId, Integer userId) throws SQLException {
        String sql = "select count(*) from pos_shift ps where ps.status='OPEN'"
                + storeFilter("ps.store_id", storeId)
                + userFilter("ps.opened_by", userId);
        return queryInt(con, sql, storeId, userId);
    }

    private int getLowStockCount(Connection con, Integer storeId) throws SQLException {
        String sql = "select count(*) from ("
                + "select sb.product_id from stock_balance sb join product p on p.id=sb.product_id "
                + "where p.active=1 and sb.status_code='ONHAND'" + storeFilter("sb.store_id", storeId)
                + " group by sb.product_id having sum(sb.qty_in_base) <= 5"
                + ") x";
        return queryInt(con, sql, storeId, null);
    }

    private List<TrendPoint> getSalesTrend(Connection con, Integer storeId, int days, Integer userId) throws SQLException {
        Map<LocalDate, BigDecimal> data = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++) {
            data.put(start.plusDays(i), BigDecimal.ZERO);
        }

        String sql = "select date(s.sold_at) as sale_date, coalesce(sum(sl.qty_in_base * sl.unit_price),0) as amount "
                + "from sale s join sale_line sl on sl.sale_id=s.id "
                + "where s.status='POSTED' and date(s.sold_at) >= ?"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId)
                + " group by date(s.sold_at) order by sale_date";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(start));
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getDate("sale_date").toLocalDate(), rs.getBigDecimal("amount"));
                }
            }
        }

        List<TrendPoint> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : data.entrySet()) {
            points.add(new TrendPoint(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private List<CategoryAmount> getTopProducts(Connection con, Integer storeId, int days, Integer userId) throws SQLException {
        List<CategoryAmount> items = new ArrayList<>();
        String sql = "select p.name, coalesce(sum(sl.qty_in_base * sl.unit_price),0) as amount "
                + "from sale s join sale_line sl on sl.sale_id=s.id join product p on p.id=sl.product_id "
                + "where s.status='POSTED' and date(s.sold_at) >= ?"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId)
                + " group by p.name order by amount desc fetch first 6 rows only";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(LocalDate.now().minusDays(days - 1L)));
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CategoryAmount(rs.getString("name"), rs.getBigDecimal("amount")));
                }
            }
        }
        return ensureNotEmpty(items, "No sales", BigDecimal.ZERO);
    }

    private List<CategoryAmount> getPaymentMix(Connection con, Integer storeId, int days, Integer userId) throws SQLException {
        List<CategoryAmount> items = new ArrayList<>();
        String sql = "select pm.name, coalesce(sum(sp.amount),0) as amount "
                + "from sale_payment sp join sale s on s.id=sp.sale_id join payment_method pm on pm.id=sp.method_id "
                + "where s.status='POSTED' and date(s.sold_at) >= ?"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId)
                + " group by pm.name order by amount desc";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(LocalDate.now().minusDays(days - 1L)));
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CategoryAmount(rs.getString("name"), rs.getBigDecimal("amount")));
                }
            }
        }
        return ensureNotEmpty(items, "No payments", BigDecimal.ZERO);
    }

    private List<CategoryAmount> getInventoryMix(Connection con, Integer storeId) throws SQLException {
        List<CategoryAmount> items = new ArrayList<>();
        String sql = "select is2.name, coalesce(sum(sb.qty_in_base),0) as qty "
                + "from stock_balance sb join inventory_status is2 on is2.code = sb.status_code "
                + "where 1=1" + storeFilter("sb.store_id", storeId)
                + " group by is2.name order by qty desc";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (storeId != null) {
                ps.setInt(1, storeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CategoryAmount(rs.getString("name"), rs.getBigDecimal("qty")));
                }
            }
        }
        return ensureNotEmpty(items, "No stock", BigDecimal.ZERO);
    }

    private List<CategoryAmount> getHourlySales(Connection con, Integer storeId, Integer userId) throws SQLException {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            data.put(String.format("%02d:00", hour), BigDecimal.ZERO);
        }
        String sql = "select hour(s.sold_at) as sale_hour, coalesce(sum(sl.qty_in_base * sl.unit_price),0) as amount "
                + "from sale s join sale_line sl on sl.sale_id=s.id "
                + "where s.status='POSTED' and date(s.sold_at)=current_date"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId)
                + " group by hour(s.sold_at) order by sale_hour";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(String.format("%02d:00", rs.getInt("sale_hour")), rs.getBigDecimal("amount"));
                }
            }
        }
        List<CategoryAmount> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
            items.add(new CategoryAmount(entry.getKey(), entry.getValue()));
        }
        return items;
    }

    private List<CategoryAmount> getMonthlyCategoryQtySales(Connection con, Integer storeId, Integer userId) throws SQLException {
        List<CategoryAmount> items = new ArrayList<>();
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonth = start.plusMonths(1);
        String sql = "select coalesce(pc.name, 'Uncategorized') as category_name, coalesce(sum(sl.qty_in_base),0) as qty "
                + "from sale s "
                + "join sale_line sl on sl.sale_id = s.id "
                + "join product p on p.id = sl.product_id "
                + "left join product_category pc on pc.id = p.category_id "
                + "where s.status='POSTED' and date(s.sold_at) >= ? and date(s.sold_at) < ?"
                + storeFilter("s.store_id", storeId)
                + userFilter("s.cashier_user_id", userId)
                + " group by coalesce(pc.name, 'Uncategorized') order by qty desc fetch first 8 rows only";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(start));
            ps.setDate(idx++, Date.valueOf(nextMonth));
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CategoryAmount(rs.getString("category_name"), rs.getBigDecimal("qty")));
                }
            }
        }
        return ensureNotEmpty(items, "No monthly category sales", BigDecimal.ZERO);
    }

    private List<LowStockItem> getLowStockItems(Connection con, Integer storeId) throws SQLException {
        List<LowStockItem> items = new ArrayList<>();
        String sql = "select p.sku, p.name, sb.status_code, sum(sb.qty_in_base) as qty "
                + "from stock_balance sb join product p on p.id=sb.product_id "
                + "where p.active=1 and sb.status_code='ONHAND'" + storeFilter("sb.store_id", storeId)
                + " group by p.sku, p.name, sb.status_code having sum(sb.qty_in_base) <= 5"
                + " order by qty asc, p.name fetch first 8 rows only";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (storeId != null) {
                ps.setInt(1, storeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new LowStockItem(
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getString("status_code"),
                            rs.getBigDecimal("qty")
                    ));
                }
            }
        }
        return items;
    }

    private String storeFilter(String columnName, Integer storeId) {
        return storeId == null ? "" : " and " + columnName + " = ?";
    }

    private String userFilter(String columnName, Integer userId) {
        return userId == null ? "" : " and " + columnName + " = ?";
    }

    private BigDecimal queryBigDecimal(Connection con, String sql, Integer storeId, Integer userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                BigDecimal value = rs.getBigDecimal(1);
                return value == null ? BigDecimal.ZERO : value;
            }
        }
    }

    private int queryInt(Connection con, String sql, Integer storeId, Integer userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            if (storeId != null) {
                ps.setInt(idx++, storeId);
            }
            if (userId != null) {
                ps.setInt(idx, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<CategoryAmount> ensureNotEmpty(List<CategoryAmount> items, String label, BigDecimal amount) {
        if (items.isEmpty()) {
            items.add(new CategoryAmount(label, amount));
        }
        return items;
    }
}
