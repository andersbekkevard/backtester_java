package strategies;

import java.util.Map;

import accounts.BarListener;
import accounts.Portfolio;
import accounts.Order;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Concrete trading strategy for AAPL based on BaseStrategy.
 */
public class Strategy extends BaseStrategy {

    public Strategy(Portfolio portfolio, Logger logger) {
        super(portfolio, logger);
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
            int amountToBuy = (int) Math.floor(portfolio.getCashReserve() / close);
            if (portfolio.getInvestedRatio() < 0.99 && portfolio.getQuantity("AAPL") == 0 && amountToBuy > 0) {
                portfolio.placeOrder(new Order("AAPL", OrderType.BUY, amountToBuy));
            }
        }
    }
}
