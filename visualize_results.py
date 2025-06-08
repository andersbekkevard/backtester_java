import argparse
import os
import pandas as pd
import matplotlib.pyplot as plt


def load_csv(path: str):
    df = pd.read_csv(path)
    df['Timestamp'] = pd.to_datetime(df['Timestamp'])
    return df


def main():
    parser = argparse.ArgumentParser(description="Plot backtest results from CSV files")
    parser.add_argument('directory', nargs='?', default='results', help='Directory with result csv files')
    parser.add_argument('--output', help='Optional PNG file to save the plot')
    args = parser.parse_args()

    files = [f for f in os.listdir(args.directory) if f.endswith('.csv')]
    if not files:
        print('No result files found in', args.directory)
        return

    plt.figure(figsize=(10, 6))
    for f in files:
        df = load_csv(os.path.join(args.directory, f))
        label = os.path.splitext(f)[0]
        plt.plot(df['Timestamp'], df['TotalValue'], label=label)

    plt.legend()
    plt.title('Portfolio Value Over Time')
    plt.xlabel('Date')
    plt.ylabel('Total Value')
    plt.tight_layout()
    if args.output:
        plt.savefig(args.output)
    else:
        plt.show()


if __name__ == '__main__':
    main()
