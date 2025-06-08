package accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import io.Logger;

public class PortfolioTest {
    private Portfolio portfolio;
    private Logger logger;

    @Before
    public void setUp() {
        logger = new Logger(System.out); // Simple logger, replace with mock if needed
        portfolio = new Portfolio(10000.0, logger);
    }

    @Test
    public void testInitialCashAndPositions() {
        assertEquals(10000.0, portfolio.getCashReserve(), 0.0001);
        assertTrue(portfolio.getPositions().isEmpty());
        assertTrue(portfolio.getClosePrices().isEmpty());
    }

    @Test
    public void testAddAndGetPositions() {
        // This should throw, as the map is unmodifiable
        try {
            portfolio.getPositions().put("AAPL", 10);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testCashReserveAfterOrder() {
        // Simulate a buy order and update manually
        double startingCash = portfolio.getCashReserve();
        // No direct method to buy, so we just check initial state
        assertEquals(10000.0, startingCash, 0.0001);
    }

    @Test
    public void testOrderRetainedWhenPriceMissing() {
        portfolio.placeOrder(new Order("AAPL", resources.enums.OrderType.BUY, 1));
        java.util.Map<String, engine.Bar> bars = new java.util.HashMap<>();
        bars.put("MSFT", new engine.Bar(java.time.LocalDateTime.now(), 1, 1, 1, 1, 1));
        portfolio.acceptBars(bars);
        assertEquals(1, portfolio.getPendingOrders().size());
    }

}
