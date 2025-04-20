package strategies;

import java.util.Map;

import accounts.Order;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Simple Stop Loss strategy: Buys and holds a stock, but sells if price drops
 * more than STOP_LOSS_PCT from entry price.
 */
public class StopLossStrategy extends Strategy {
	public static final double STOP_LOSS_PCT = 0.05;
	private static final String SYMBOL = "AAPL";

	private Double entryPrice = null;

	public StopLossStrategy(Portfolio portfolio, Logger logger) {
		super(portfolio, logger);
	}

	@Override
	protected void onBars(Map<String, Bar> bars) {
		Bar bar = bars.get(SYMBOL);
		if (bar == null)
			return;
		double close = bar.close();
		if (close <= 0.0)
			return;

		int position = portfolio.getQuantity(SYMBOL);

		if (position == 0) {
			// Not invested, buy as much as possible
			int qty = (int) Math.floor(portfolio.getCashReserve() / close);
			if (qty > 0) {
				portfolio.placeOrder(new Order(SYMBOL, OrderType.BUY, qty));
				entryPrice = close;
				logger.info("StopLossStrategy: Bought " + qty + " shares at " + close);
			}
		} else {
			// Check for stop loss
			if (entryPrice != null && close <= entryPrice * (1.0 - STOP_LOSS_PCT)) {
				portfolio.placeOrder(new Order(SYMBOL, OrderType.SELL, position));
				logger.info("StopLossStrategy: Stop loss triggered. Sold all at " + close);
				entryPrice = null;
			}
		}
	}
}
