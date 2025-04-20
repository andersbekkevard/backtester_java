package app;

import accounts.Portfolio;
import engine.StockExchange;
import io.Logger;
import strategies.Strategy;
import strategies.Strategy;

/**
 * Orchestrates the setup and running of the backtest.
 * Handles all dependency wiring, running, and (eventually) result reporting.
 */
public class BacktestOrchestrator {
    private final Logger logger;
    private final StockExchange exchange;
    private final Portfolio portfolio;
    private final Strategy strategy;

    public BacktestOrchestrator(Logger logger) {
        this.logger = logger;
        this.exchange = StockExchange.demoExchange(logger);
        this.portfolio = new Portfolio(10000, logger);
        this.strategy = new Strategy(portfolio, logger);
        wireDependencies();
    }

    private void wireDependencies() {
        exchange.addPortfolio(portfolio);
        exchange.addStrategy(strategy);
    }

    public void runBacktest() {
        exchange.run();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public StockExchange getExchange() {
        return exchange;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Prints a comprehensive backtest result summary.
     */
    public void onFinish() {
        logger.infoNoFlag("==============================");
        logger.infoNoFlag("      BACKTEST RESULT SUMMARY  ");
        logger.infoNoFlag("==============================");
        logger.infoNoFlag(String.format("Starting Cash:      $%.2f", portfolio.getStartingCash()));
        logger.infoNoFlag(String.format("Final Portfolio Value: $%.2f", portfolio.getValue()));
        logger.infoNoFlag(String.format("Cash Reserve:       $%.2f", portfolio.getCashReserve()));
        logger.infoNoFlag("\n--- Positions ---");
        for (String ticker : portfolio.getPositions().keySet()) {
            int qty = portfolio.getQuantity(ticker);
            double price = portfolio.getClosePrices().getOrDefault(ticker, 0.0);
            logger.infoNoFlag(String.format("%s: %d shares @ $%.2f", ticker, qty, price));
        }
        logger.infoNoFlag("\n--- Performance ---");
        double start = portfolio.getStartingCash();
        double end = portfolio.getValue();
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
        logger.infoNoFlag("==============================");
    }

    /**
     * Plots the tracked portfolio returns using XChart.
     */
    public void plotPortfolioReturns() {
        portfolio.getHistoryTracker().plotReturnsChart("Portfolio Value Over Time");
    }
}

