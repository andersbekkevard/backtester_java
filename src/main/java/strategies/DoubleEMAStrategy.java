package strategies;

import java.util.Map;

import accounts.Order;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Concrete trading strategy for AAPL based on BaseStrategy.
 */
public class DoubleEMAStrategy extends Strategy {
	private static final int EMA_PERIOD = 20;
	private static final String STOCK1 = "AAPL";
	private static final String STOCK2 = "MSFT";

	private final EMASignal signal;
	private Double prevCloseAAPL = null;
	private Double prevEmaAAPL = null;
	private Double prevCloseMSFT = null;
	private Double prevEmaMSFT = null;

	public DoubleEMAStrategy(Portfolio portfolio, Logger logger) {
		super(portfolio, logger);
		this.signal = new EMASignal(this, EMA_PERIOD);
	}

	@Override
	protected void onBars(Map<String, Bar> bars) {
		signal.update(bars);

		if (bars == null || bars.get(STOCK1) == null || bars.get(STOCK2) == null) {
			return;
		}

		// Get close and EMA for both stocks
		Bar aaplBar = bars.get(STOCK1);
		Bar msftBar = bars.get(STOCK2);
		double closeAAPL = aaplBar.close();
		double closeMSFT = msftBar.close();
		double emaAAPL = signal.getEma(STOCK1);
		double emaMSFT = signal.getEma(STOCK2);
		if (closeAAPL <= 0.0 || closeMSFT <= 0.0 || Double.isNaN(emaAAPL) || Double.isNaN(emaMSFT)) {
			return;
		}

		// Determine which stock to trade: the one where (current EMA - previous close) is largest
		// If prevClose is null for either, fall back to EMA comparison
		String winner, loser;
		if (prevCloseAAPL != null && prevCloseMSFT != null) {
			double diffAAPL = emaAAPL - prevCloseAAPL;
			double diffMSFT = emaMSFT - prevCloseMSFT;
			if (diffAAPL > diffMSFT) {
				winner = STOCK1;
				loser = STOCK2;
			} else {
				winner = STOCK2;
				loser = STOCK1;
			}
		} else {
			winner = emaAAPL > emaMSFT ? STOCK1 : STOCK2;
			loser = emaAAPL > emaMSFT ? STOCK2 : STOCK1;
		}

		// Always liquidate the loser if we hold it
		int loserPosition = portfolio.getQuantity(loser);
		if (loserPosition > 0) {
			portfolio.placeOrder(new Order(loser, OrderType.SELL, loserPosition));
			logger.info("Sold all shares of loser stock: " + loser);
		}

		// Winner trade logic (like EMAStrategy)
		int winnerPosition = portfolio.getQuantity(winner);
		double close = winner.equals(STOCK1) ? closeAAPL : closeMSFT;
		double ema = winner.equals(STOCK1) ? emaAAPL : emaMSFT;
		Double prevClose = winner.equals(STOCK1) ? prevCloseAAPL : prevCloseMSFT;
		Double prevEma = winner.equals(STOCK1) ? prevEmaAAPL : prevEmaMSFT;

		if (prevClose != null && prevEma != null) {
			// Buy signal: Only go long if:
			// (1) Previous close was below previous EMA
			// (2) Current close is above current EMA
			// (3) Not already invested
			if (prevClose < prevEma && close > ema && winnerPosition == 0) {
				int amountToBuy = (int) Math.floor(portfolio.getCashReserve() / close);
				if (amountToBuy > 0) {
					portfolio.placeOrder(new Order(winner, OrderType.BUY, amountToBuy));
					logger.info("Buy signal: Close crossed above EMA for " + winner + ". Bought " + amountToBuy + " shares.");
				}
			}
			// Sell signal: Only sell if:
			// (1) Previous close was above previous EMA
			// (2) Current close is below current EMA
			// (3) Currently invested
			else if (prevClose > prevEma && close < ema && winnerPosition > 0) {
				portfolio.placeOrder(new Order(winner, OrderType.SELL, winnerPosition));
				logger.info("Sell signal: Close crossed below EMA for " + winner + ". Sold all shares.");
			}
		}

		// Update previous values for both stocks
		if (winner.equals(STOCK1)) {
			prevCloseAAPL = closeAAPL;
			prevEmaAAPL = emaAAPL;
			prevCloseMSFT = closeMSFT;
			prevEmaMSFT = emaMSFT;
		} else {
			prevCloseAAPL = closeAAPL;
			prevEmaAAPL = emaAAPL;
			prevCloseMSFT = closeMSFT;
			prevEmaMSFT = emaMSFT;
		}
	}
}
