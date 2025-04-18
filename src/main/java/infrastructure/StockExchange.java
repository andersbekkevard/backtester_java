package infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The stock exchange is the object that runs the simulation. It has a list of
 * portfolios as listeners, and notifies them at every priceupdate
 * Stock data, like tickes and prices are held in Stock-objects
 * It uses the CSVparser helper class to read the csv files
 */
public class StockExchange {
	/* ================================= Static ================================= */
	private record PriceChange(String ticker, double oldPrice, double newPrice) {
	}

	private static final String CLOSE_HEADER = "Close";
	public static final String AAPL_TICKER = "AAPL";
	public static final String MSFT_TICKER = "MSFT";
	public static final String AAPL_PATH = "C:\\Users\\Anders\\Code\\java\\backtester_java\\src\\main\\java\\data\\aapl.csv";
	public static final String MSFT_PATH = "C:\\Users\\Anders\\Code\\java\\backtester_java\\src\\main\\java\\data\\msft.csv";

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
	private final HashMap<Stock, CSVparser> stockMap = new HashMap<>();
	private final List<Portfolio> portfolios = new ArrayList<>();
	private final List<PriceChange> priceChanges = new ArrayList<>();
	private boolean isRunning = false;
	private int frequency = 1;

	/* =========================== Initializing phase ========================== */
	/**
	 * Adds a stock record to the hashmap, and reads the first price
	 */
	public void addStock(String ticker, String dataPath) throws IOException {
		if (isRunning)
			throw new IllegalStateException();

		Stock stock = new Stock(ticker, dataPath);
		if (!stockMap.containsKey(stock))
			stockMap.put(stock, new CSVparser(stock.getDataPath()));

		// Calls nextPrice() once to get an initial prize for portfolio purchases
		nextPrice(stock);
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

	public boolean step() {
		if (allFinishedParsing())
			return false;
		for (Stock s : stockMap.keySet()) {
			nextPrice(s);
		}

		onBarClose();
		return true;
	}

	private void onBarClose() {
		for (PriceChange p : priceChanges) {
			notifyListeners(p.ticker(), p.oldPrice(), p.newPrice());
		}
		priceChanges.clear();
	}

	public void run() {
		while (step()) {

		}
	}

	public double getStockPrice(String ticker) {
		Stock stock = getStockFromTicker(ticker);
		return stock.getPrice();
	}

	/*
	 * ============================== Private Helpers =============================
	 */

	/**
	 * Calls the CSVparser corresponding to the Stock object for the next price and
	 * updates the Stock price
	 * If price was changed, listeners are notified
	 * 
	 * @param stock
	 */
	private void nextPrice(Stock stock) {
		if (!stockMap.containsKey(stock))
			throw new IllegalArgumentException();

		for (int i = 0; i < frequency; i++) {
			boolean wentToNext = stockMap.get(stock).goToNext();
			if (!wentToNext) {
				stock.setFinishedParsing(true);
				return;
			}
		}
		// Set new stockprice in map, and store priceChange
		double oldPrice = stock.getPrice();
		double newPrice = stockMap.get(stock).getValue(CLOSE_HEADER);
		stock.setPrice(newPrice);
		if (oldPrice != newPrice) {
			stock.setPrice(newPrice);
			priceChanges.add(new PriceChange(stock.getTicker(), oldPrice, newPrice));
		}
	}

	/**
	 * Notifies all listeners of a change in a stock price
	 * 
	 * @param ticker
	 * @param oldPrice
	 * @param newPrice
	 */
	private void notifyListeners(String ticker, double oldPrice, double newPrice) {
		portfolios.stream().forEach(p -> p.priceUpdate(ticker, oldPrice, newPrice));
	}

	/**
	 * Helper method to know wheter all CSV-parsers are finished parsing
	 * 
	 */
	private boolean allFinishedParsing() {
		return stockMap.keySet().stream().allMatch(Stock::isFinishedParsing);
	}

	/**
	 * Helper method that returns the Stock object corresponding to a ticker if it
	 * exists in the stockMap
	 * 
	 * @param ticker
	 * @return
	 */
	private Stock getStockFromTicker(String ticker) {
		if (ticker == null || ticker.equals(""))
			throw new IllegalArgumentException();
		if (!stockMap.keySet().stream().anyMatch(s -> s.getTicker().equals(ticker)))
			throw new IllegalArgumentException("Exchange doesnt contain stock");

		return stockMap.keySet().stream().filter(s -> ticker.equals(s.getTicker())).findFirst().get();
	}
}
