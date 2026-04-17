package com.aldrin.ensarium.dashboard;


import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public final class JavaFxChartFactory {

    private static final String ROOT_STYLE = "-fx-background-color: white;";
    private static final String CHART_STYLE = "-fx-background-color: transparent; -fx-padding: 6;";
    private static final String EMPTY_STYLE = "-fx-background-color: white; -fx-alignment: center;";
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final NumberFormat DECIMAL = NumberFormat.getNumberInstance(Locale.US);

    private JavaFxChartFactory() {
    }

    public static JFXPanel createSalesTrendChart(List<TrendPoint> trend, DateTimeFormatter dateFormat) {
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> panel.setScene(new Scene(wrap(buildSalesTrendChart(trend, dateFormat)), Color.TRANSPARENT)));
        return panel;
    }

    public static JFXPanel createCurrencyVerticalBarChart(List<CategoryAmount> data) {
        return createVerticalBarChart(data, JavaFxChartFactory::formatCurrency);
    }

    public static JFXPanel createQuantityVerticalBarChart(List<CategoryAmount> data) {
        return createVerticalBarChart(data, JavaFxChartFactory::formatQuantity);
    }

    public static JFXPanel createCurrencyHorizontalBarChart(List<CategoryAmount> data) {
        return createHorizontalBarChart(data, JavaFxChartFactory::formatCurrency);
    }

    public static JFXPanel createCurrencyPieChart(List<CategoryAmount> data) {
        return createPieChart(data, JavaFxChartFactory::formatCurrency);
    }

    public static JFXPanel createQuantityPieChart(List<CategoryAmount> data) {
        return createPieChart(data, JavaFxChartFactory::formatQuantity);
    }

    private static JFXPanel createVerticalBarChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> panel.setScene(new Scene(wrap(buildVerticalBarChart(data, formatter)), Color.TRANSPARENT)));
        return panel;
    }

    private static JFXPanel createHorizontalBarChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> panel.setScene(new Scene(wrap(buildHorizontalBarChart(data, formatter)), Color.TRANSPARENT)));
        return panel;
    }

    private static JFXPanel createPieChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> panel.setScene(new Scene(wrap(buildPieChart(data, formatter)), Color.TRANSPARENT)));
        return panel;
    }

    private static Node buildSalesTrendChart(List<TrendPoint> trend, DateTimeFormatter dateFormat) {
        if (trend == null || trend.isEmpty()) {
            return buildEmptyState("No sales trend data");
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelRotation(-20);
        yAxis.setForceZeroInRange(false);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setStyle(CHART_STYLE);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (TrendPoint point : trend) {
            String label = point.date() == null ? "" : point.date().format(dateFormat);
            double value = point.amount() == null ? 0d : point.amount().doubleValue();
            XYChart.Data<String, Number> chartData = new XYChart.Data<>(label, value);
            attachTooltip(chartData, label + "\n" + formatCurrency(value));
            series.getData().add(chartData);
        }
        chart.getData().add(series);
        return createChartHost(chart);
    }

    private static Node buildVerticalBarChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        if (data == null || data.isEmpty()) {
            return buildEmptyState("No chart data");
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelRotation(-25);
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCategoryGap(14);
        chart.setBarGap(4);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setStyle(CHART_STYLE);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (CategoryAmount item : data) {
            String fullLabel = safe(item.category(), "N/A");
            String label = shorten(fullLabel, 14);
            double value = item.amount() == null ? 0d : item.amount().doubleValue();
            XYChart.Data<String, Number> chartData = new XYChart.Data<>(label, value);
            attachTooltip(chartData, fullLabel + "\n" + formatter.apply(value));
            series.getData().add(chartData);
        }
        chart.getData().add(series);
        return createChartHost(chart);
    }

    private static Node buildHorizontalBarChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        if (data == null || data.isEmpty()) {
            return buildEmptyState("No hourly sales data");
        }

        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        xAxis.setForceZeroInRange(true);

        BarChart<Number, String> chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCategoryGap(10);
        chart.setBarGap(4);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setStyle(CHART_STYLE);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        for (CategoryAmount item : data) {
            String label = safe(item.category(), "N/A");
            double value = item.amount() == null ? 0d : item.amount().doubleValue();
            XYChart.Data<Number, String> chartData = new XYChart.Data<>(value, label);
            attachTooltip(chartData, label + "\n" + formatter.apply(value));
            series.getData().add(chartData);
        }
        chart.getData().add(series);
        return createChartHost(chart);
    }

    private static Node buildPieChart(List<CategoryAmount> data, Function<Double, String> formatter) {
        if (data == null || data.isEmpty()) {
            return buildEmptyState("No pie chart data");
        }

        ObservableList<PieChart.Data> items = FXCollections.observableArrayList();
        for (CategoryAmount item : data) {
            double value = item.amount() == null ? 0d : item.amount().doubleValue();
            String name = safe(item.category(), "N/A");
            PieChart.Data pieData = new PieChart.Data(name, value);
            attachTooltip(pieData, name + "\n" + formatter.apply(value));
            items.add(pieData);
        }

        PieChart chart = new PieChart(items);
        chart.setAnimated(false);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setClockwise(true);
        chart.setLabelLineLength(14);
        chart.setStyle(CHART_STYLE);
        return createChartHost(chart);
    }

    private static <X, Y> void attachTooltip(XYChart.Data<X, Y> data, String text) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> installTooltip(newNode, text));
        if (data.getNode() != null) {
            installTooltip(data.getNode(), text);
        }
    }

    private static void attachTooltip(PieChart.Data data, String text) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> installTooltip(newNode, text));
        if (data.getNode() != null) {
            installTooltip(data.getNode(), text);
        }
    }

    private static void installTooltip(Node node, String text) {
        if (node == null) {
            return;
        }
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(50));
        Tooltip.install(node, tooltip);
    }

    private static Node createChartHost(Node chart) {
        VBox box = new VBox(chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        box.setPadding(new Insets(0));
        box.setStyle(ROOT_STYLE);
        return box;
    }

    private static StackPane wrap(Node node) {
        StackPane root = new StackPane(node);
        root.setPadding(new Insets(0));
        root.setStyle(ROOT_STYLE);
        return root;
    }

    private static StackPane buildEmptyState(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.web("#6B7280"));
        label.setFont(Font.font(14));

        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(12));
        pane.setStyle(EMPTY_STYLE);
        return pane;
    }

    private static String formatCurrency(double value) {
        return CURRENCY.format(value);
    }

    private static String formatQuantity(double value) {
        return DECIMAL.format(value);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
