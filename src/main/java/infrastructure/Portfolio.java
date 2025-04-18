package infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A portfolio represents an actors assets as well as cash reserves
 */
public class Portfolio implements PriceUpdateListener {

	/* ================================= Fields ================================= */
	private final StockExchange exchange;
	private final double startingCash;
	private double cashReserve;
	private final List<StockHolding> stocks = new ArrayList<>();

	/* =============================== Constructor ============================== */
	public Portfolio(StockExchange exchange, double startingCash) {
		this.startingCash = startingCash;
		this.cashReserve = startingCash;
		this.exchange = exchange;
		exchange.addPortfolio(this);
	}

	/* ============================= Public methods ============================= */
	public void buyStock(String ticker, double amount) {
		double price = exchange.getStockPrice(ticker);
		if (cashReserve - (price * amount) < 0)
			throw new IllegalStateException("Insufficient funds");

		stocks.add(new StockHolding(ticker, price, amount));
	}

	public double getValue() {
		return cashReserve + stocks.stream().mapToDouble(StockHolding::getValue).sum();
	}

	/**
	 * This method finds stock and then notifies if present
	 * Doing a naive priceUpdate on all StockHoldings would also work
	 * as the ticker verification logic is implemented there aswell
	 * Although I think this is more efficient
	 */
	@Override
	public void priceUpdate(String ticker, double oldPrice, double newPrice) {
		Optional<StockHolding> holding = getHoldingFromTicker(ticker);
		if (holding.isEmpty())
			return;
		holding.get().priceUpdate(ticker, oldPrice, newPrice);
	}

	/* ============================= Private helpers ============================ */
	/**
	 * Returns an optional of the StockHolding corresponding to the ticker if it
	 * exists in the portfolio
	 * 
	 * @param ticker
	 * @return
	 */
	private Optional<StockHolding> getHoldingFromTicker(String ticker) {
		if (ticker == null || ticker.equals(""))
			throw new IllegalArgumentException();
		return stocks.stream().filter(s -> ticker.equals(s.getTicker())).findFirst();
	}
}
