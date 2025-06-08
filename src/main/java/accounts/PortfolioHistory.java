package accounts;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.XYStyler;

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

        /**
         * Write the tracked portfolio values to a CSV file with two columns:
         * timestamp and total value.
         *
         * @param path output path for the csv file
         * @throws IOException if writing fails
         */
        public void saveToCsv(Path path) throws IOException {
                Files.createDirectories(path.getParent());
                DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path))) {
                        pw.println("Timestamp,TotalValue");
                        for (Snapshot s : snapshots) {
                                String ts = s.getTimestamp().format(fmt);
                                pw.println(ts + "," + s.getTotalValue());
                        }
                }
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

	/* ============================= XChart Plotting ============================ */
	public void plotReturnsChart(String title) {
		List<Date> dates = snapshots.stream()
				.map(PortfolioHistory.Snapshot::getTimestamp)
				.map(ts -> Date.from(ts.atZone(ZoneId.systemDefault()).toInstant()))
				.collect(Collectors.toList());

		List<Double> values = snapshots.stream()
				.map(PortfolioHistory.Snapshot::getTotalValue)
				.collect(Collectors.toList());

		// Create Chart
		XYChart chart = new XYChartBuilder()
				.width(900)
				.height(600)
				.title(title)
				.xAxisTitle("Date")
				.yAxisTitle("Total Value")
				.build();

		// Style Chart
		XYStyler styler = chart.getStyler();
		styler.setLegendVisible(false);
		styler.setDatePattern("yyyy-MM-dd");
		styler.setMarkerSize(4);
		styler.setChartTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		styler.setAxisTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		styler.setAxisTickLabelsFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		styler.setPlotBackgroundColor(Color.WHITE);
		styler.setPlotGridLinesVisible(true);
		styler.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

		// Add data series
		chart.addSeries("Total Value", dates, values);

                // Display chart unless running headless
                if (!GraphicsEnvironment.isHeadless()) {
                        try {
                                if (SwingUtilities.isEventDispatchThread()) {
                                        new Thread(() -> new SwingWrapper<>(chart).displayChart()).start();
                                } else {
                                        new SwingWrapper<>(chart).displayChart();
                                }
                        } catch (HeadlessException ignore) {
                                // ignore when no display is available
                        }
                }
	}

	/**
	 * Plots the tracked portfolio returns of multiple strategies using XChart on
	 * the same chart.
	 * 
	 * @param histories List of PortfolioHistory objects
	 * @param labels    List of labels for each portfolio
	 * @param title     Chart title
	 */
	public static void plotMultipleReturnsChart(
			List<PortfolioHistory> histories,
			List<String> labels,
			String title) {
		if (histories.size() != labels.size() || histories.isEmpty()) {
			System.out.println("Histories and labels must be same size and non-empty.");
			return;
		}
		XYChart chart = new XYChartBuilder()
				.width(1000)
				.height(600)
				.title(title)
				.xAxisTitle("Date")
				.yAxisTitle("Total Value")
				.build();
		XYStyler styler = chart.getStyler();
		styler.setLegendVisible(true);
		styler.setLegendPosition(org.knowm.xchart.style.Styler.LegendPosition.OutsideE);
		styler.setDatePattern("yyyy-MM-dd");
		styler.setMarkerSize(4);
		styler.setChartTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		styler.setAxisTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		styler.setAxisTickLabelsFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		styler.setPlotBackgroundColor(Color.WHITE);
		styler.setPlotGridLinesVisible(true);
		styler.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
		for (int i = 0; i < histories.size(); i++) {
			PortfolioHistory h = histories.get(i);
			String label = labels.get(i);
			List<Snapshot> snaps = h.getSnapshots();
			if (snaps.isEmpty())
				continue;
			List<Date> dates = snaps.stream()
					.map(Snapshot::getTimestamp)
					.map(ts -> Date.from(ts.atZone(ZoneId.systemDefault()).toInstant()))
					.collect(Collectors.toList());
			List<Double> values = snaps.stream().map(Snapshot::getTotalValue).collect(Collectors.toList());
			chart.addSeries(label, dates, values);
		}
                if (!GraphicsEnvironment.isHeadless()) {
                        try {
                                if (SwingUtilities.isEventDispatchThread()) {
                                        new Thread(() -> new SwingWrapper<>(chart).displayChart()).start();
                                } else {
                                        new SwingWrapper<>(chart).displayChart();
                                }
                        } catch (HeadlessException ignore) {
                                // ignore when no display is available
                        }
                }
        }
}
