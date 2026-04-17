package com.aldrin.ensarium.dashboard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class DashboardData {

    public final BigDecimal todaySales;
    public final int todayTransactions;
    public final BigDecimal avgTicket;
    public final BigDecimal todayProfit;
    public final int openShifts;
    public final int lowStockCount;
    public final List<TrendPoint> salesTrend;
    public final List<CategoryAmount> topProducts;
    public final List<CategoryAmount> paymentMix;
    public final List<CategoryAmount> inventoryMix;
    public final List<CategoryAmount> hourlySales;
    public final List<CategoryAmount> monthlyCategoryQtySales;
    public final List<LowStockItem> lowStockItems;

    public DashboardData(BigDecimal todaySales, int todayTransactions, BigDecimal avgTicket, BigDecimal todayProfit,
            int openShifts, int lowStockCount, List<TrendPoint> salesTrend, List<CategoryAmount> topProducts,
            List<CategoryAmount> paymentMix, List<CategoryAmount> inventoryMix, List<CategoryAmount> hourlySales,
            List<CategoryAmount> monthlyCategoryQtySales, List<LowStockItem> lowStockItems) {
        this.todaySales = todaySales;
        this.todayTransactions = todayTransactions;
        this.avgTicket = avgTicket;
        this.todayProfit = todayProfit;
        this.openShifts = openShifts;
        this.lowStockCount = lowStockCount;
        this.salesTrend = salesTrend;
        this.topProducts = topProducts;
        this.paymentMix = paymentMix;
        this.inventoryMix = inventoryMix;
        this.hourlySales = hourlySales;
        this.monthlyCategoryQtySales = monthlyCategoryQtySales;
        this.lowStockItems = lowStockItems;
    }

    public static DashboardData empty() {
        return new DashboardData(
                BigDecimal.ZERO,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
