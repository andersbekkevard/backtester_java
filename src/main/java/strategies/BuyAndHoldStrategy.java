package strategies;

import java.util.Map;

import accounts.Order;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;
import resources.enums.OrderType;

/**
 * Simple buy-and-hold strategy: invests all cash in a single ticker at the
 * first opportunity, then tries to buy more when possible.
 */
public class BuyAndHoldStrategy extends Strategy {
    private final String ticker;
    private boolean invested = false;

    public BuyAndHoldStrategy(Portfolio portfolio, Logger logger, String ticker) {
        super(portfolio, logger);
        this.ticker = ticker;
    }

    @Override
    protected void onBars(Map<String, Bar> bars) {
        if (invested)
            return;
        Bar bar = bars.get(ticker);

        double close = bar.close();
        if (close > 0.0) {
            int amountToBuy = (int) Math.floor(portfolio.getCashReserve() / close);
            if (amountToBuy > 0) {
                portfolio.placeOrder(new Order(ticker, OrderType.BUY, amountToBuy));
                invested = true;
                logger.info("BuyAndHold: Bought and holding " + amountToBuy + " shares of " + ticker);
            }
        } else {
            logger.error("BuyAndHold: Invalid close price for " + ticker + ": " + close);
        }

    }
}
