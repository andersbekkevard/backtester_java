package app;

import infrastructure.Portfolio;
import infrastructure.StockExchange;
import infrastructure.Strategy;

public class App {

	public static void main(String[] args) {
		StockExchange ex = StockExchange.demoExchange();
		Portfolio p = new Portfolio(10000);
		Strategy s = new Strategy();
		ex.addPortfolio(p);
		ex.addStrategy(s);
		s.setPortfolio(p);

		ex.run();

	}
}
