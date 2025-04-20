# Backtester Java

A modular backtesting engine for financial trading strategies, written in Java.
This is still work in progress, but it is already functional.

## Overview

This project allows you to test and compare trading strategies on historical data. The system is modular, so stock universes, strategies and signals can easily be switched. The system takes CSV-files on Date-OHLCV format. They can easily be downloaded from Yahoo Finance using import_data.py. Just remember to add the new tickers and paths to the StockExchange

## Features

- **Strategy Framework**: Implement and plug in custom strategies using existing signals if desired
- **Portfolio Simulation**: Simulate portfolio value, positions, and trades over time
- **Multiple Built-in Strategies**: Includes EMA, Momentum, Stop Loss, and Buy & Hold strategies
- **Performance Analytics**: Summarizes returns, cash, positions, and more
- **Visualization**: Plots portfolio value over time using XChart
- **Logging**: Detailed logging for debugging and analysis

## Project Structure

- `src/main/java/app/` – Orchestration and entry point (`App.java`, `BacktestOrchestrator.java`)
- `src/main/java/strategies/` – Strategy implementations (e.g., `EMAStrategy`, `MomentumStrategy`)
- `src/main/java/accounts/` – Portfolio, Order, and related classes
- `src/main/java/io/` – Logging utilities
- `src/main/java/engine/` – Market data and simulation engine

## Getting Started

### Prerequisites

- Java 21
- Maven

### Build and Run

Clone repo and build maven. Run App.java

## Usage Example

Modify `App.java` to configure which strategies to test and with what parameters. Example strategies include:

- **Buy & Hold**
- **EMA Crossover**
- **Momentum**
- **Stop Loss**

After running, the orchestrator will print portfolio summaries and plot performance charts.

## Extending

To add your own strategy:

1. Create a new class in `strategies/` extending `Strategy`.
2. Implement the `onBars` method with your trading logic.
3. Register your strategy in `App.java` using `BacktestOrchestrator.StrategyConfig`.

## AI Usage

This project has been created with the help of ChatGPT-4.1, especially for writing printing and visualization logic, as well as implementing several strategies.
