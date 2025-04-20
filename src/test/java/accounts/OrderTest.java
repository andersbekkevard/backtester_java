package accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import resources.enums.OrderType;

public class OrderTest {
    @Test
    public void testOrderCreation() {
        Order order = new Order("AAPL", OrderType.BUY, 10);
        assertEquals("AAPL", order.getTicker());
        assertEquals(OrderType.BUY, order.getOrderType());
        assertEquals(10, order.getQuantity());
    }

    @Test
    public void testOrderEquality() {
        Order o1 = new Order("AAPL", OrderType.SELL, 5);
        Order o2 = new Order("AAPL", OrderType.SELL, 5);
        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    public void testOrderInequality() {
        Order o1 = new Order("AAPL", OrderType.SELL, 5);
        Order o2 = new Order("AAPL", OrderType.BUY, 5);
        Order o3 = new Order("AAPL", OrderType.SELL, 10);
        Order o4 = new Order("MSFT", OrderType.SELL, 5);
        assertNotEquals(o1, o2);
        assertNotEquals(o1, o3);
        assertNotEquals(o1, o4);
    }

    @Test
    public void testToString() {
        Order order = new Order("AAPL", OrderType.BUY, 10);
        String str = order.toString();
        assertTrue(str.contains("AAPL"));
        assertTrue(str.contains("BUY"));
        assertTrue(str.contains("10"));
    }
}
