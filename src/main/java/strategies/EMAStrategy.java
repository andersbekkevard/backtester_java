package strategies;

import java.util.Map;

import accounts.Order;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Concrete EMA trading strategy for AAPL
 */
public class EMAStrategy extends Strategy {
	private static final int EMA_PERIOD = 20;

        private final EMASignal signal;
	private Double prevClose = null;
	private Double prevEma = null;

        public EMAStrategy(Portfolio portfolio, Logger logger) {
                super(portfolio, logger);
                this.signal = new EMASignal(this, EMA_PERIOD);
                addSignal(signal);
        }

	@Override
	protected void onBars(Map<String, Bar> bars) {
                Bar aaplBar = bars.get("AAPL");
		if (aaplBar != null) {
			double close = aaplBar.close();
			if (close <= 0.0) {
				logger.error("AAPL close price is invalid: " + close);
				return;
			}
			double ema = signal.getEma("AAPL");
			if (Double.isNaN(ema)) {
				logger.error("EMA not calculated for AAPL");
				return;
			}

			int position = portfolio.getQuantity("AAPL");

			// Only trade if we have a previous value to compare to
			if (prevClose != null && prevEma != null) {
				// Buy signal: previous close below EMA, current close above EMA, and not
				// already invested
				if (prevClose < prevEma && close > ema && position == 0) {
					int amountToBuy = (int) Math.floor(portfolio.getCashReserve() / close);
					if (amountToBuy > 0) {
						portfolio.placeOrder(new Order("AAPL", OrderType.BUY, amountToBuy));
						logger.info("Buy signal: Close crossed above EMA. Bought " + amountToBuy + " shares.");
					}
				}
				// Sell signal: previous close above EMA, current close below EMA, and currently
				// invested
				else if (prevClose > prevEma && close < ema && position > 0) {
					portfolio.placeOrder(new Order("AAPL", OrderType.SELL, position));
					logger.info("Sell signal: Close crossed below EMA. Sold all shares.");
				}
			}

			// Update previous values
			prevClose = close;
			prevEma = ema;
		}
	}
}
