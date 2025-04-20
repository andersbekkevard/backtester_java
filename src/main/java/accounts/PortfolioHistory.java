package accounts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tracks portfolio snapshots over time, including positions and total value.
 */
public class PortfolioHistory {
    private final List<Snapshot> snapshots = new ArrayList<>();

    /**
     * Record a snapshot of positions and total portfolio value at a given time.
     * 
     * @param timestamp  the time of the snapshot
     * @param positions  a map of ticker to position size
     * @param totalValue the total portfolio value at this time
     */
    public void record(LocalDateTime timestamp, Map<String, Integer> positions, double totalValue) {
        snapshots.add(new Snapshot(timestamp, positions, totalValue));
    }

    public List<Snapshot> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    public static class Snapshot {
        private final LocalDateTime timestamp;
        private final Map<String, Integer> positions;
        private final double totalValue;

        public Snapshot(LocalDateTime timestamp, Map<String, Integer> positions, double totalValue) {
            this.timestamp = timestamp;
            this.positions = positions;
            this.totalValue = totalValue;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Integer> getPositions() {
            return positions;
        }

        public double getTotalValue() {
            return totalValue;
        }
    }

    // --- XChart Plotting ---
    public void plotReturnsChart(String title) {
        List<Snapshot> snaps = getSnapshots();
        if (snaps.isEmpty()) {
            System.out.println("No portfolio history to plot.");
            return;
        }
        // Plot ALL datapoints for high resolution
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.util.List<java.util.Date> xData = new java.util.ArrayList<>();
        java.util.List<Double> yData = new java.util.ArrayList<>();
        for (Snapshot s : snaps) {
            java.util.Date dt = java.util.Date.from(s.getTimestamp().atZone(zone).toInstant());
            xData.add(dt);
            yData.add(s.getTotalValue());
        }
        // Switch to XYChart (line chart) with high resolution and readable x axis labels
        // Switch to XYChart (line chart) for proper time series rendering
        org.knowm.xchart.XYChart chart = new org.knowm.xchart.XYChartBuilder()
            .width(1200).height(600)
            .title(title)
            .xAxisTitle("Date")
            .yAxisTitle("Portfolio Value")
            .build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setDatePattern("yy-MM-dd HH:mm");
        chart.getStyler().setXAxisTickMarkSpacingHint(180);
        chart.getStyler().setAxisTickLabelsFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
        chart.addSeries("Portfolio Value", xData, yData);
        new org.knowm.xchart.SwingWrapper<>(chart).displayChart();
    }
}
