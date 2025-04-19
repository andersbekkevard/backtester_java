package infrastructure;

import java.util.Map;

import resources.enums.OrderType;

public class Strategy implements BarListener {
	private Portfolio portfolio;
	private Signal signal;

	public void setPortfolio(Portfolio portfolio) {
		if (portfolio == null)
			throw new IllegalArgumentException();
		if (portfolio.equals(this.portfolio))
			return;
		this.portfolio = portfolio;
		if (!this.equals(portfolio.getStrategy())) {
			portfolio.setStrategy(this);
		}
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	@Override
	public void acceptBars(Map<String, Bar> bars) {
		try {
			if (portfolio.getInvestedRatio() < 0.99) {
				int amountToBuy = (int) Math.floor(portfolio.getCashReserve() / bars.get("AAPL").close());
				portfolio.placeOrder(new Order("AAPL", OrderType.BUY, amountToBuy));

			}
		} catch (Exception e) {
			System.out.println("Couldnt invest. bars look like this: " + bars);
		}
	}

}
