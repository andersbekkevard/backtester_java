package infrastructure;

public interface PriceUpdateListener {
	void priceUpdate(String ticker, double oldPrice, double newPrice);
}
