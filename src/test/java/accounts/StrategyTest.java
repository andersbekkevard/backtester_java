package accounts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import engine.Bar;
import io.Logger;
import strategies.Strategy;

public class StrategyTest {
    private Portfolio portfolio;
    private Logger logger;
    private Strategy strategy;

    @Before
    public void setUp() {
        logger = new Logger(System.out); // Replace with a mock if you want to capture logs
        portfolio = new Portfolio(1000.0, logger);
        strategy = new Strategy(portfolio, logger);
    }

    @Test
    public void testNoBarsDoesNothing() {
        strategy.acceptBars(Collections.emptyMap());
        // Should not throw, should not place orders
        assertTrue(portfolio.getPendingOrders().isEmpty());
    }

    @Test
    public void testInvalidBarSkipsOrder() {
        Map<String, Bar> bars = new HashMap<>();
        bars.put("AAPL", new Bar(0.0, 0.0, 0.0, 0.0, 0.0)); // Invalid close
        strategy.acceptBars(bars);
        assertTrue(portfolio.getPendingOrders().isEmpty());
    }

}
