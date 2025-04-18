package infrastructure;

/**
 * The Stock object represents a tradable asset on a StockExchange. It is
 * associated with a csv found at the location dataPath
 */
public class Stock {
	private final String ticker;
	private final String dataPath;
	private double price = 0;
	private boolean isFinishedParsing = false;

	public Stock(String ticker, String dataPath) {
		this.ticker = ticker;
		this.dataPath = dataPath;
	}

	public String getTicker() {
		return ticker;
	}

	public String getDataPath() {
		return dataPath;
	}

	public double getPrice() {
		return price;
	}

	public boolean isFinishedParsing() {
		return isFinishedParsing;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setFinishedParsing(boolean isFinishedParsing) {
		this.isFinishedParsing = isFinishedParsing;
	}

}