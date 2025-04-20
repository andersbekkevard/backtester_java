package app;

import io.Logger;

public class App {

	public static void main(String[] args) {
		Logger logger = new Logger(System.out);
		BacktestOrchestrator orchestrator = new BacktestOrchestrator(logger);
		orchestrator.runBacktest();
		orchestrator.onFinish();
		orchestrator.plotPortfolioReturns();
	}
}
