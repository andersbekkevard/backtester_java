package infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CSVparser {
	private final String dataPath;
	private Scanner scanner;
	private List<String> headers;
	private Map<String, Double> thisLine = new HashMap<>();

	public CSVparser(String dataPath) throws IOException {
		this.dataPath = dataPath;
		this.scanner = new Scanner(Path.of(dataPath));
		this.scanner.useDelimiter("\n");
		populateHeaders();
		goToNext();
	}

	private void populateHeaders() {
		if (this.headers != null)
			throw new IllegalStateException();

		String[] headerStrings = scanner.next().split(",");
		this.headers = Arrays.stream(headerStrings).map(String::trim).collect(Collectors.toList());
	}

	public boolean goToNext() {
		if (!scanner.hasNext()) {
			return false;
		}
		thisLine.clear();
		String[] tokens = scanner.next().split(",");
		for (int i = 1; i < headers.size(); i++) {
			thisLine.put(headers.get(i), Double.valueOf(tokens[i]));
		}
		return true;
	}

	public double getValue(String header) {
		if (!headers.contains(header))
			throw new IllegalArgumentException();
		return thisLine.get(header);
	}

	/**
	 * This method returns a bar object corresponding to a stock.
	 * Assumes csv data is on OHLVC format
	 * 
	 * @return
	 */
	public Bar getBar() {
		try {
			double open = getValue("Open");
			double high = getValue("High");
			double low = getValue("Low");
			double close = getValue("Close");
			double volume = getValue("Volume");
			return new Bar(open, high, low, close, volume);

		} catch (Exception e) {
			throw new IllegalStateException("Data not on OHLCV-format");
		}

	}
}
