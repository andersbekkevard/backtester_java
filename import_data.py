import os
import argparse
import yfinance as yf
import pandas as pd

DEFAULT_TICKERS = ["AAPL", "MSFT", "SPY"]
DEFAULT_START = "2000-01-01"
DEFAULT_END = "2010-01-01"

OUTPUT_DIR = os.path.join("src", "main", "java", "resources", "data")
os.makedirs(OUTPUT_DIR, exist_ok=True)


def fetch_stock_data(symbol: str, start_date, end_date) -> pd.DataFrame:
    """Fetch OHLCV stock data using yfinance, only Date-OHLCV."""
    ticker = yf.Ticker(symbol)
    df = ticker.history(start=start_date, end=end_date, auto_adjust=True)
    if df.empty:
        return df
    keep_cols = ["Open", "High", "Low", "Close", "Volume"]
    df = df[keep_cols]
    df = df.reset_index()
    df["Date"] = df["Date"].dt.date
    df = df.set_index("Date")
    return df


def main():
    parser = argparse.ArgumentParser(description="Fetch historical stock data")
    parser.add_argument("tickers", nargs="*", default=DEFAULT_TICKERS, help="List of tickers")
    parser.add_argument("--start", default=DEFAULT_START, help="Start date YYYY-MM-DD")
    parser.add_argument("--end", default=DEFAULT_END, help="End date YYYY-MM-DD")
    parser.add_argument("--output", default=OUTPUT_DIR, help="Output directory")
    args = parser.parse_args()

    os.makedirs(args.output, exist_ok=True)

    for ticker in args.tickers:
        print(f"Fetching {ticker}...")
        df = fetch_stock_data(ticker, args.start, args.end)
        file_path = os.path.join(args.output, f"{ticker.lower()}.csv")
        if not df.empty:
            with open(file_path, "w", newline="") as f:
                f.write(df.to_csv())
            print(f"Saved: {file_path}")
        else:
            print(f"No data for {ticker}")


if __name__ == "__main__":
    main()
