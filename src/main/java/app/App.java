package app;

import java.util.ArrayList;
import java.util.List;

import io.Logger;
import strategies.BuyAndHoldStrategy;
import strategies.EMAStrategy;
import strategies.MomentumStrategy;
import strategies.StopLossStrategy;

public class App {

	public static void main(String[] args) {
		Logger logger = new Logger(System.out);
		List<BacktestOrchestrator.StrategyConfig> configs = new ArrayList<>();
		configs.add(new BacktestOrchestrator.StrategyConfig(
				"SNP",
				(p, l) -> new BuyAndHoldStrategy(p, l, "SNP")));

		configs.add(new BacktestOrchestrator.StrategyConfig(
				"Buy & Hold",
				(p, l) -> new BuyAndHoldStrategy(p, l, "AAPL")));

		configs.add(new BacktestOrchestrator.StrategyConfig(
				"EMA",
				(p, l) -> new EMAStrategy(p, l)));

		configs.add(new BacktestOrchestrator.StrategyConfig(
				"Stop Loss",
				(p, l) -> new StopLossStrategy(p, l)));

		configs.add(new BacktestOrchestrator.StrategyConfig(
				"Momentum",
				(p, l) -> new MomentumStrategy(p, l)));

		BacktestOrchestrator orchestrator = new BacktestOrchestrator(logger, configs,
				1000.0);
		orchestrator.runBacktest();
		orchestrator.onFinish();
		orchestrator.plotPortfolioReturns();
	}
}
