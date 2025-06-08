package app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import accounts.Portfolio;
import accounts.PortfolioHistory;
import engine.StockExchange;
import io.Logger;
import strategies.Strategy;

public class BacktestOrchestrator {
	private static final String SEPARATOR = "=================================";

	private final List<Portfolio> portfolios = new ArrayList<>();
	private final List<Strategy> strategies = new ArrayList<>();
	private final List<String> labels = new ArrayList<>();
	private final Logger logger;
	private final StockExchange exchange;

	public static class StrategyConfig {
		public final String label;
		public final BiFunction<Portfolio, Logger, Strategy> factory;

		public StrategyConfig(String label, BiFunction<Portfolio, Logger, Strategy> factory) {
			this.label = label;
			this.factory = factory;
		}
	}

	public BacktestOrchestrator(Logger logger, List<StrategyConfig> strategyConfigs, double startingCash) {
		this.logger = logger;
		this.exchange = StockExchange.demoExchange(logger);
		for (StrategyConfig config : strategyConfigs) {
			Portfolio p = new Portfolio(startingCash, logger);
			Strategy s = config.factory.apply(p, logger);
			portfolios.add(p);
			strategies.add(s);
			labels.add(config.label);
		}
		wireDependencies();
	}

        private void wireDependencies() {
                for (Portfolio p : portfolios) {
                        exchange.addBarListener(p);
                }
                for (Strategy s : strategies) {
                        exchange.addBarListener(s);
                }
        }

	public void runBacktest() {
		exchange.run();
	}

	public StockExchange getExchange() {
		return exchange;
	}

	public Logger getLogger() {
		return logger;
	}

	/**
	 * Prints a comprehensive backtest result summary.
	 */
        public void onFinish() {
                logger.infoNoFlag(SEPARATOR);
                logger.infoNoFlag("      BACKTEST RESULT SUMMARY");
                logger.infoNoFlag(SEPARATOR);

                java.io.File resultsDir = new java.io.File("results");
                resultsDir.mkdirs();

                for (int i = 0; i < portfolios.size(); i++) {
                        logger.infoNoFlag("\n" + SEPARATOR);
                        logger.infoNoFlag(String.format(" STRATEGY: %-40s ", labels.get(i)));
                        logger.infoNoFlag(SEPARATOR);
                        printPortfolioSummary(portfolios.get(i));
                        try {
                                String safe = labels.get(i).replaceAll("[^a-zA-Z0-9_-]", "_");
                                java.nio.file.Path out = java.nio.file.Paths.get("results", safe + ".csv");
                                portfolios.get(i).getHistoryTracker().saveToCsv(out);
                        } catch (Exception e) {
                                logger.error("Failed to write history", e);
                        }
                }

		logger.infoNoFlag("==============================");
	}

	private void printPortfolioSummary(Portfolio portfolio) {
		logger.infoNoFlag(String.format("Starting Cash:      $%.2f", portfolio.getStartingCash()));
		logger.infoNoFlag(String.format("Final Portfolio Value: $%.2f", portfolio.getTotalValue()));
		logger.infoNoFlag(String.format("Cash Reserve:       $%.2f", portfolio.getCashReserve()));
		logger.infoNoFlag("\n--- Positions ---");
		for (String ticker : portfolio.getPositions().keySet()) {
			int qty = portfolio.getQuantity(ticker);
			double price = portfolio.getClosePrices().getOrDefault(ticker, 0.0);
			logger.infoNoFlag(String.format("%s: %d shares @ $%.2f", ticker, qty, price));
		}
		logger.infoNoFlag("\n--- Performance ---");
		double start = portfolio.getStartingCash();
		double end = portfolio.getTotalValue();
		double ret = (end - start) / start * 100.0;
		logger.infoNoFlag(String.format("Total Return:       %.2f%%", ret));
		// Print history summary
		java.util.List<Double> history = portfolio.getHistory();
		if (history.size() > 1) {
			double min = history.stream().min(Double::compare).orElse(start);
			double max = history.stream().max(Double::compare).orElse(end);
			logger.infoNoFlag(String.format("Min Value:          $%.2f", min));
			logger.infoNoFlag(String.format("Max Value:          $%.2f", max));
			logger.infoNoFlag(String.format("Num Steps:          %d", history.size()));
		}
	}

	/**
	 * Plots the tracked portfolio returns using XChart.
	 */
	public void plotPortfolioReturns() {
		List<PortfolioHistory> histories = new ArrayList<>();
		for (Portfolio p : portfolios) {
			histories.add(p.getHistoryTracker());
		}
		PortfolioHistory.plotMultipleReturnsChart(histories, labels, "Portfolio Value Over Time: All Strategies");
	}
}
