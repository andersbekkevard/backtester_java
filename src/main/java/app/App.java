package app;

import infrastructure.Portfolio;
import infrastructure.StockExchange;

public class App {

	public static void main(String[] args) {
		StockExchange ex = StockExchange.demoExchange();
		Portfolio p = new Portfolio(ex, 10000);
		p.buyStock("AAPL", 100);
		p.buyStock("MSFT", 100);
		System.out.println(p.getValue());
		ex.run();
		System.out.println(p.getValue());
	}
}
