package infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The stock exchange is the object that runs the simulation. It has a list of
 * portfolios as listeners, at every bar with OHLCV data of each ticker
 * It uses the CSVparser helper class to read the csv files
 */
public class StockExchange {
	/* ================================= Static ================================= */

	public static final String AAPL_TICKER = "AAPL";
	public static final String MSFT_TICKER = "MSFT";
	public static final String AAPL_PATH = "src\\main\\java\\resources\\data\\aapl.csv";
	public static final String MSFT_PATH = "src\\main\\java\\resources\\data\\msft.csv";

	public static StockExchange demoExchange() {
		try {
			StockExchange ex = new StockExchange();
			ex.addStock(AAPL_TICKER, AAPL_PATH);
			ex.addStock(MSFT_TICKER, MSFT_PATH);
			return ex;

		} catch (IOException e) {
			System.err.println("Couldnt find path");
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
	 */
	private final List<Strategy> strategies = new ArrayList<>();
	private final List<Portfolio> portfolios = new ArrayList<>();

	/*
	 * Barmap is filled up each day, and all listeners are notified before moving on
	 */
	private final Map<String, Bar> barMap = new HashMap<>();

	/*
	 * These are simple fields that govern the process of a StockExchange
	 */
	private boolean isRunning = false;
	private int frequency = 1;

	/* =========================== Initializing phase ========================== */
	/**
	 * Adds a stock record to the hashmap, and reads the first price
	 */
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
		if (!portfolios.contains(portfolio))
			portfolios.add(portfolio);
	}

	/**
	 * Adds a strategy to be notified
	 * 
	 * @param strategy
	 */
	public void addStrategy(Strategy strategy) {
		if (!strategies.contains(strategy))
			strategies.add(strategy);
	}

	/* =============================== Event Loop =============================== */
	public void run() {
		isRunning = true;
		while (step()) {
		}
		System.out.println("Finished parsing files");
		System.out.println("Final portfolio value is: ");
		portfolios.stream().forEach(p -> System.out.println(p.getHistory().get(p.getHistory().size() - 10)));
	}

	public boolean step() {
		if (allFinishedParsing())
			return false;
		for (String ticker : stockMap.keySet()) {
			tick(ticker);
		}
		onBarClose();
		return true;
	}

	private void tick(String ticker) {
		if (!stockMap.containsKey(ticker))
			throw new IllegalArgumentException();

		// Lap through the unwanted bars. If any are absent we are finished parsing
		CSVparser parser = stockMap.get(ticker);
		for (int i = 0; i < frequency; i++) {
			boolean wentToNext = parser.goToNext();
			if (!wentToNext) {
				isFinished.put(ticker, true);
				return;
			}
		}
		Bar bar = stockMap.get(ticker).getBar();
		barMap.put(ticker, bar);
	}

	/**
	 * Handles notification and clearing up the maps
	 */
	private void onBarClose() {
		Map<String, Bar> outputMap = Collections.unmodifiableMap(barMap);
		portfolios.stream().forEach(p -> p.acceptBars(outputMap));
		strategies.stream().forEach(s -> s.acceptBars(outputMap));
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
