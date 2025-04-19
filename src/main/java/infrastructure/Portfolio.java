package infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resources.enums.OrderType;

/**
 * A portfolio represents an actors assets as well as cash reserves
 */
public class Portfolio implements BarListener {

	/* ================================= Fields ================================= */
	private Strategy strategy;
	private final double startingCash;
	private double cashReserve;
	private final Map<String, Integer> positions = new HashMap<>();
	private final Map<String, Double> closePrices = new HashMap<>();
	private final List<Order> pendingOrders = new ArrayList<>();
	/**
	 * History is now kept in a dumb list. In the future I plan to extend this
	 * functionalty so each portfolio has its own tracker that keeps track of
	 * position sizes at different dates and can calculate things like vol, sharpe
	 * etc
	 */
	private final List<Double> history = new ArrayList<>();

	/* =============================== Constructor ============================== */
	public Portfolio(double startingCash) {
		this.startingCash = startingCash;
		this.cashReserve = startingCash;
	}

	public void setStrategy(Strategy strategy) {
		if (strategy == null)
			throw new IllegalArgumentException();
		if (strategy.equals(this.strategy))
			return;
		this.strategy = strategy;
		if (!this.equals(strategy.getPortfolio())) {
			strategy.setPortfolio(this);
		}
	}

	public Strategy getStrategy() {
		return strategy;
	}

	private double equityValue() {
		double value = 0;
		for (String ticker : positions.keySet()) {
			value += positions.getOrDefault(ticker, 0) * closePrices.getOrDefault(ticker, 0.0);
		}
		return value;
	}

	private void executeOrders() {
		for (Order o : pendingOrders) {
			try {
				executeSingleOrder(o);
			} catch (IllegalStateException e) {
				System.err.println("Insufficient funds. Order: " + o + " was not executed");
			}
		}
		pendingOrders.clear();
	}

	private void executeSingleOrder(Order o) {
		if (!closePrices.containsKey(o.getTicker()))
			throw new IllegalArgumentException("Dont have acces to price of this order");

		switch (o.getOrderType()) {
			case OrderType.BUY -> {
				if (o.getQuantity() * closePrices.get(o.getTicker()) > cashReserve)
					throw new IllegalStateException("Dont have enough cash to place order");

				positions.put(o.getTicker(), positions.getOrDefault(o.getTicker(), 0) + o.getQuantity());
				cashReserve -= o.getQuantity() * closePrices.get(o.getTicker());
			}

			case OrderType.SELL -> {
				if (o.getQuantity() > positions.getOrDefault(o.getTicker(), 0))
					throw new IllegalStateException("Dont have enough holdings in this stock");

				positions.put(o.getTicker(), positions.getOrDefault(o.getTicker(), 0) - o.getQuantity());
				cashReserve += o.getQuantity() * closePrices.get(o.getTicker());
			}
			default -> throw new AssertionError();
		}
	}

	public List<Double> getHistory() {
		return history;
	}

	@Override
	public void acceptBars(Map<String, Bar> barMap) {
		history.add(getValue());
		closePrices.clear();
		barMap.keySet().stream().forEach(t -> closePrices.put(t, barMap.get(t).close()));
		executeOrders();
	}

	/* ======================== Portfolio to Strategy API ======================= */
	public void placeOrder(Order order) {
		pendingOrders.add(order);
	}

	public double getCashReserve() {
		return cashReserve;
	}

	public double getValue() {
		return cashReserve + equityValue();
	}

	public int getQuantity(String ticker) {
		return positions.getOrDefault(ticker, 0);
	}

	public double getStartingCash() {
		return startingCash;
	}

	/**
	 * Returns a double between 0 and 1 signifying the percentage of our funds that
	 * are invested
	 * 
	 * @return
	 */
	public double getInvestedRatio() {
		return equityValue() / getValue();
	}
}
