package infrastructure;

/**
 * A StockHolding represents a portfolios position in a certain stock. The
 * Portfolio object can delegate the priceUpdate call to each of the
 * StockHolding objects
 */
public class StockHolding implements PriceUpdateListener {
	private final String ticker;
	private double entryPrice;
	private double currentPrice;
	private double amount;

	public StockHolding(String ticker, double entryPrice, double amount) {
		this.ticker = ticker;
		this.entryPrice = entryPrice;
		this.amount = amount;
	}

	@Override
	public void priceUpdate(String ticker, double oldPrice, double newPrice) {
		if (this.ticker.equals(ticker))
			this.currentPrice = newPrice;
	}

	public double getValue() {
		return currentPrice * amount;
	}

	public double getReturn() {
		return currentPrice / entryPrice - 1;
	}

	public String getTicker() {
		return ticker;
	}

}
