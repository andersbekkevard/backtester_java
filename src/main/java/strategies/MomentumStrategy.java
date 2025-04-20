package strategies;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import accounts.Order;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Simple Momentum Strategy: Each bar, buy the stock (AAPL or MSFT) with the
 * highest N-bar return.
 * Only hold one stock at a time. Rebalance when the leader changes.
 */
public class MomentumStrategy extends Strategy {
	private static final String STOCK1 = "AAPL";
	private static final String STOCK2 = "MSFT";
	public static final int LOOKBACK = 20; // lookback window in bars

	private final Queue<Double> aaplPrices = new LinkedList<>();
	private final Queue<Double> msftPrices = new LinkedList<>();
	private final boolean stayInCashIfBothNegative;

	public MomentumStrategy(Portfolio portfolio, Logger logger) {
		this(portfolio, logger, false);
	}

	public MomentumStrategy(Portfolio portfolio, Logger logger, boolean stayInCashIfBothNegative) {
		super(portfolio, logger);
		this.stayInCashIfBothNegative = stayInCashIfBothNegative;
	}

	@Override
	protected void onBars(Map<String, Bar> bars) {
		Bar aaplBar = bars.get(STOCK1);
		Bar msftBar = bars.get(STOCK2);
		if (aaplBar == null || msftBar == null)
			return;
		double aaplClose = aaplBar.close();
		double msftClose = msftBar.close();
		if (aaplClose <= 0.0 || msftClose <= 0.0)
			return;

		// Update rolling windows
		aaplPrices.add(aaplClose);
		msftPrices.add(msftClose);
		if (aaplPrices.size() > LOOKBACK)
			aaplPrices.poll();
		if (msftPrices.size() > LOOKBACK)
			msftPrices.poll();
		if (aaplPrices.size() < LOOKBACK || msftPrices.size() < LOOKBACK)
			return;

		double aaplReturn = aaplClose / aaplPrices.peek() - 1.0;
		double msftReturn = msftClose / msftPrices.peek() - 1.0;

		// Stay in cash if both are negative and flag is enabled
		if (stayInCashIfBothNegative && aaplReturn < 0 && msftReturn < 0) {
			int posA = portfolio.getQuantity(STOCK1);
			int posM = portfolio.getQuantity(STOCK2);
			if (posA > 0) {
				portfolio.placeOrder(new Order(STOCK1, OrderType.SELL, posA));
				logger.info("MomentumStrategy: Both negative, sold all AAPL");
			}
			if (posM > 0) {
				portfolio.placeOrder(new Order(STOCK2, OrderType.SELL, posM));
				logger.info("MomentumStrategy: Both negative, sold all MSFT");
			}
			return;
		}

		String winner = aaplReturn > msftReturn ? STOCK1 : STOCK2;
		String loser = aaplReturn > msftReturn ? STOCK2 : STOCK1;

		// Liquidate loser if held
		int loserPosition = portfolio.getQuantity(loser);
		if (loserPosition > 0) {
			portfolio.placeOrder(new Order(loser, OrderType.SELL, loserPosition));
			logger.info("MomentumStrategy: Sold all shares of loser: " + loser);
		}

		// Buy winner if not already fully invested
		int winnerPosition = portfolio.getQuantity(winner);
		if (winnerPosition == 0) {
			int qty = (int) Math.floor(portfolio.getCashReserve() / (winner.equals(STOCK1) ? aaplClose : msftClose));
			if (qty > 0) {
				portfolio.placeOrder(new Order(winner, OrderType.BUY, qty));
				logger.info("MomentumStrategy: Bought " + qty + " shares of winner: " + winner);
			}
		}
	}
}
