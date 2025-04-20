package strategies;

import java.util.Map;

import engine.Bar;

/**
 * Abstract base class for trading signals.
 * Signals are updated with new market data (bars) and can interact with a
 * BaseStrategy.
 */
public abstract class Signal {
    protected final Strategy strategy;

    public Signal(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Update the signal with the latest bars.
     * 
     * @param bars Map of symbol to Bar
     */
    public abstract void update(Map<String, Bar> bars);
}
