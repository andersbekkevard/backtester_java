package accounts;

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
		return "Order [ticker=" + ticker + ", type=" + type + ", quantity=" + quantity + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Order order = (Order) o;
		return quantity == order.quantity &&
				ticker.equals(order.ticker) &&
				type == order.type;
	}

	@Override
	public int hashCode() {
		int result = ticker.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + quantity;
		return result;
	}
}
