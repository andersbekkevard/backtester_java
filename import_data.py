import os
import yfinance as yf
import pandas as pd

TICKERS = ["AAPL", "MSFT", "SPY"]
START_DATE = "2000-01-01"
END_DATE = "2010-01-01"

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
    for ticker in TICKERS:
        print(f"Fetching {ticker}...")
        df = fetch_stock_data(ticker, START_DATE, END_DATE)
        file_path = os.path.join(OUTPUT_DIR, f"{ticker.lower()}.csv")
        if not df.empty:
            with open(file_path, "w", newline="") as f:
                f.write(df.to_csv())
            print(f"Saved: {file_path}")
        else:
            print(f"No data for {ticker}")


if __name__ == "__main__":
    main()
