package strategies;

import java.util.Map;
import java.util.Objects;

import accounts.BarListener;
import accounts.Portfolio;
import engine.Bar;
import io.Logger;

/**
 * Abstract base class for trading strategies. Handles bar validation and error
 * catching.
 */
public abstract class Strategy implements BarListener {
    protected final Portfolio portfolio;
    protected final Logger logger;

    public Strategy(Portfolio portfolio, Logger logger) {
        this.portfolio = Objects.requireNonNull(portfolio);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public final void acceptBars(Map<String, Bar> bars) {
        try {
            if (bars == null || bars.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Bar> entry : bars.entrySet()) {
                if (entry.getValue() == null) {
                    logger.error(entry.getKey() + " bar is null");
                    return;
                }
            }
            onBars(bars);
        } catch (Exception e) {
            logger.error("Error in strategy processing bars: " + bars, e);
        }
    }

    /**
     * Called after validation for subclasses to implement strategy logic.
     */
    protected abstract void onBars(Map<String, Bar> bars);
}
