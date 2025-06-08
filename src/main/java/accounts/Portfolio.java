package accounts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * A portfolio represents an actors assets as well as cash reserves
 */
public class Portfolio implements BarListener {

	/* ================================= Fields ================================= */
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
	private final PortfolioHistory historyTracker = new PortfolioHistory();
	private final Logger logger;

	/* =============================== Constructor ============================== */

	public Portfolio(double startingCash, Logger logger) {
		this.startingCash = startingCash;
		this.cashReserve = startingCash;
		this.logger = Objects.requireNonNull(logger);
	}

       private void executeOrders() {
               List<Order> remaining = new ArrayList<>();
               for (Order o : pendingOrders) {
                       try {
                               if (!executeSingleOrder(o)) {
                                       remaining.add(o);
                               }
                       } catch (IllegalStateException e) {
                               logger.error("Insufficient funds. Order: " + o + " was not executed", e);
                               remaining.add(o);
                       }
               }
               pendingOrders.clear();
               pendingOrders.addAll(remaining);
       }

       private boolean executeSingleOrder(Order o) {
               if (!closePrices.containsKey(o.getTicker())) {
                       logger.error("Dont have access to price of this order: " + o);
                       return false;
               }

		switch (o.getOrderType()) {
			case OrderType.BUY -> {
                               if (o.getQuantity() * closePrices.get(o.getTicker()) > cashReserve) {
                                       logger.error("Dont have enough cash to place order: " + o);
                                       return false;
                               }
                               positions.put(o.getTicker(), positions.getOrDefault(o.getTicker(), 0) + o.getQuantity());
                               cashReserve -= o.getQuantity() * closePrices.get(o.getTicker());
                               }

			case OrderType.SELL -> {
                               if (o.getQuantity() > positions.getOrDefault(o.getTicker(), 0)) {
                                       logger.error("Dont have enough holdings in this stock to sell: " + o);
                                       return false;
                               }
                               positions.put(o.getTicker(), positions.getOrDefault(o.getTicker(), 0) - o.getQuantity());
                               cashReserve += o.getQuantity() * closePrices.get(o.getTicker());
                               }
                       default -> throw new AssertionError();
               }
               return true;
       }

	/**
	 * Records snapshot in historytracker, then executes orders and finally updates
	 * the closePrices. Assumes all bars come in at the same timestamp
	 */
	@Override
	public void acceptBars(Map<String, Bar> barMap) {
		if (barMap == null || barMap.isEmpty())
			throw new IllegalArgumentException("Can't send empty barMap to portfolio");

		LocalDateTime timestamp = barMap.values().stream().findFirst().get().timestamp();
		Map<String, Integer> positionsSnapshot = Collections.unmodifiableMap(new HashMap<>(positions));
		double totalValue = getTotalValue();
		historyTracker.record(timestamp, positionsSnapshot, totalValue);

		// Keep simple value history for debugging
		history.add(totalValue);

		executeOrders();
		closePrices.clear();
		for (String t : barMap.keySet()) {
			Bar bar = barMap.get(t);
			if (bar != null) {
				closePrices.put(t, bar.close());
			}
		}
	}

	/* ======================== Portfolio to Strategy API ======================= */
	public void placeOrder(Order order) {
		pendingOrders.add(order);
	}

	public double getCashReserve() {
		return cashReserve;
	}

	public double getEquityValue() {
		double value = 0;
		for (String ticker : positions.keySet()) {
			value += positions.getOrDefault(ticker, 0) * closePrices.getOrDefault(ticker, 0.0);
		}
		return value;
	}

	public double getTotalValue() {
		return cashReserve + getEquityValue();
	}

	public int getQuantity(String ticker) {
		return positions.getOrDefault(ticker, 0);
	}

	public double getStartingCash() {
		return startingCash;
	}

	public List<Order> getPendingOrders() {
		return pendingOrders;
	}

	/**
	 * Returns a double between 0 and 1 signifying the percentage of our funds that
	 * are invested
	 */
	public double getInvestedRatio() {
		return getEquityValue() / getTotalValue();
	}

	/* ============================ Generalt getters ============================ */
	public Map<String, Integer> getPositions() {
		return Collections.unmodifiableMap(positions);
	}

	public Map<String, Double> getClosePrices() {
		return Collections.unmodifiableMap(closePrices);
	}

	public PortfolioHistory getHistoryTracker() {
		return historyTracker;
	}

	public List<Double> getHistory() {
		return history;
	}
}
