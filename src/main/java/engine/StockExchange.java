package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import accounts.BarListener;
import accounts.Portfolio;
import io.CSVparser;
import io.Logger;
import strategies.Strategy;

/**
 * The stock exchange is the object that runs the simulation. It has a list of
 * portfolios as listeners, at every bar with OHLCV data of each ticker
 * It uses the CSVparser helper class to read the csv files
 */
public class StockExchange {
	/* ================================= Static ================================= */

	public static final String AAPL_TICKER = "AAPL";
	public static final String MSFT_TICKER = "MSFT";
	public static final String SPY_TICKER = "SPY";
	public static final String AAPL_PATH = "src\\main\\java\\resources\\data\\aapl.csv";
	public static final String MSFT_PATH = "src\\main\\java\\resources\\data\\msft.csv";
	public static final String SPY_PATH = "src\\main\\java\\resources\\data\\spy.csv";

	public static StockExchange demoExchange(Logger logger) {
		try {
			StockExchange ex = new StockExchange(logger);
			ex.addStock(AAPL_TICKER, AAPL_PATH);
			ex.addStock(MSFT_TICKER, MSFT_PATH);
			ex.addStock(SPY_TICKER, SPY_PATH);
			return ex;

		} catch (IOException e) {
			logger.error("Couldnt find path", e);
			return null;
		}
	}

	/* ================================= Fields ================================= */
	/*
	 * Two maps for keeping track of stocks. One relates the ticker to the CSVparser
	 * The other one keeps track of what parsers are finished parsing
	 */
	private final Map<String, CSVparser> stockMap = new HashMap<>();
	private final Map<String, Boolean> isFinished = new HashMap<>();

	/*
	 * Both portfolios and strategies register as listeners at the exchange
	 * It is important that a strategy is initialized with a portfolio
	 * This is handled by the BactestOrchestrator
	 */
        private final List<BarListener> listeners = new ArrayList<>();

	/*
	 * Barmap is filled up each day, and all listeners are notified before moving on
	 */
	private final Map<String, Bar> barMap = new HashMap<>();

	/*
	 * These are simple fields that govern the process of a StockExchange
	 */
	private boolean isRunning = false;
	private int frequency = 1;
	private final Logger logger;

	/* =============================== Constructor ============================== */
	public StockExchange(Logger logger) {
		this.logger = Objects.requireNonNull(logger);
	}

	/* =========================== Initializing phase ========================== */
	public void addStock(String ticker, String dataPath) throws IOException {
		if (isRunning)
			throw new IllegalStateException();

		stockMap.putIfAbsent(ticker, new CSVparser(dataPath));
		isFinished.putIfAbsent(ticker, false);
	}

	/**
	 * Sets the frequency of PriceUpdateListener updates.
	 * Signifies the amount of lines (which in this case corresponds to days)
	 * to wait before notifying listeners
	 * 
	 * @param frequency
	 */
	public void setFrequency(int frequency) {
		if (isRunning)
			throw new IllegalStateException();
		if (frequency < 1)
			throw new IllegalArgumentException("Can't have negative frequency");
		this.frequency = frequency;
	}

	/**
	 * Adds a portfolio to be notified
	 * 
	 * @param portfolio
	 */
        public void addPortfolio(Portfolio portfolio) {
                addBarListener(portfolio);
        }

	/**
	 * Adds a strategy to be notified
	 * 
	 * @param strategy
	 */
        public void addStrategy(Strategy strategy) {
                addBarListener(strategy);
        }

        public void addBarListener(BarListener listener) {
                if (!listeners.contains(listener))
                        listeners.add(listener);
        }

	/* =============================== Event Loop =============================== */
	public void run() {
		isRunning = true;
		while (step()) {
		}
		logger.info("Finished parsing files");
	}

	public boolean step() {
		if (allFinishedParsing())
			return false;
		List<String> tickers = new ArrayList<>(stockMap.keySet());
		for (String ticker : tickers) {
			tick(ticker);
		}
		onBarClose();
		return true;
	}

	private void tick(String ticker) {
		CSVparser parser = stockMap.get(ticker);
		if (parser == null)
			return;

		for (int i = 0; i < frequency; i++) {
			if (parser.goToNext())
				continue;
			handleParserEnd(ticker, parser);
			return;
		}

		parser = stockMap.get(ticker);
		if (parser == null)
			return;

		Bar bar = parser.getBar();
		if (bar == null) {
			barMap.remove(ticker);
		} else {
			barMap.put(ticker, bar);
		}
	}

	private void handleParserEnd(String ticker, CSVparser parser) {
		isFinished.put(ticker, true);
		try {
			parser.close();
		} catch (Exception ignored) {
		}
		stockMap.remove(ticker);
		barMap.remove(ticker);
	}

	/**
	 * Handles notification and clearing up the maps
	 */
	private void onBarClose() {
		// Remove any tickers that are finished
		isFinished.forEach((ticker, finished) -> {
		});

		for (String ticker : isFinished.keySet()) {
			if (isFinished.get(ticker))
				barMap.remove(ticker);
		}
		// Remove bars with invalid close prices (zero or negative)
		barMap.entrySet().removeIf(entry -> entry.getValue().close() <= 0.0);
		// If no valid bars after cleanup, skip notifying listeners
		if (barMap.isEmpty()) {
			return;
		}
                Map<String, Bar> outputMap = Collections.unmodifiableMap(barMap);
                listeners.forEach(l -> l.acceptBars(outputMap));
                barMap.clear();
        }

	/**
	 * Helper method to know wheter all CSV-parsers are finished parsing
	 * 
	 */
	private boolean allFinishedParsing() {
		return isFinished.values().stream().allMatch(finished -> finished);
	}
}
