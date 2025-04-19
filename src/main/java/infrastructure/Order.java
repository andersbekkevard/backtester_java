package infrastructure;

import resources.enums.OrderType;

public class Order {
	private final String ticker;
	private final OrderType type;
	private final int quantity;

	public Order(String ticker, OrderType type, int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException("Cant make negative orders");
		this.ticker = ticker;
		this.type = type;
		this.quantity = quantity;
	}

	public String getTicker() {
		return ticker;
	}

	public OrderType getOrderType() {
		return type;
	}

	public int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "Order [ticker=" + ticker + ", quantity=" + quantity + "]";
	}

}
