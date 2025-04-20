package strategies;

import java.util.HashMap;
import java.util.Map;

import engine.Bar;

/**
 * Signal that calculates the Exponential Moving Average (EMA) for each symbol.
 */
public class EMASignal extends Signal {
    private final Map<String, Double> emaMap = new HashMap<>();
    private final Map<String, Boolean> initialized = new HashMap<>();
    private final double alpha;
    private final int period;

    public EMASignal(Strategy strategy, int period) {
        super(strategy);
        this.period = period;
        this.alpha = 2.0 / (period + 1);
    }

    @Override
    public void update(Map<String, Bar> bars) {
        for (String symbol : bars.keySet()) {
            double close = bars.get(symbol).close();
            if (!initialized.getOrDefault(symbol, false)) {
                emaMap.put(symbol, close);
                initialized.put(symbol, true);
            } else {
                double prevEma = emaMap.get(symbol);
                double newEma = alpha * close + (1 - alpha) * prevEma;
                emaMap.put(symbol, newEma);
            }
        }
    }

    /**
     * Get the current EMA for a symbol.
     */
    public Double getEma(String symbol) {
        return emaMap.getOrDefault(symbol, Double.NaN);
    }
}
