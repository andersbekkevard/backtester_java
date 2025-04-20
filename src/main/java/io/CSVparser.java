package io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import engine.Bar;

/**
 * Robust CSV parser for OHLCV data files.
 * Handles malformed lines, missing data, and ensures resources are managed.
 */
public class CSVparser implements Closeable {
	private final String dataPath;
	private Scanner scanner;
	private List<String> headers;
	private Map<String, Double> thisLine = new HashMap<>();
	private boolean headersPopulated = false;

	public CSVparser(String dataPath) throws IOException {
		this.dataPath = dataPath;
		try {
			this.scanner = new Scanner(Path.of(dataPath));
			this.scanner.useDelimiter("\n");
			populateHeaders();
			goToNext();
		} catch (Exception e) {
			close();
			throw new IOException("Failed to initialize CSVparser: " + e.getMessage(), e);
		}
	}

	/**
	 * Reads and stores the CSV header row.
	 * Throws if headers already populated or header row is malformed.
	 */
	private void populateHeaders() {
		if (headersPopulated)
			throw new IllegalStateException("Headers already populated");
		if (!scanner.hasNext())
			throw new IllegalStateException("CSV file is empty, no header row");
		String headerLine = scanner.next();
		String[] headerStrings = headerLine.split(",");
		if (headerStrings.length < 2)
			throw new IllegalArgumentException("Header row must have at least two columns");
		this.headers = Arrays.stream(headerStrings).map(String::trim).collect(Collectors.toList());
		headersPopulated = true;
	}

	/**
	 * Advances to the next data line, parsing values into thisLine map.
	 * Handles missing or malformed data gracefully.
	 * 
	 * @return true if a new line was read, false if EOF
	 */
	public boolean goToNext() {
		thisLine.clear();
		while (scanner.hasNext()) {
			String line = scanner.next();
			if (line.trim().isEmpty())
				continue; // skip blank lines
			String[] tokens = line.split(",");
			for (int i = 1; i < headers.size(); i++) {
				if (i < tokens.length) {
					try {
						thisLine.put(headers.get(i), Double.valueOf(tokens[i]));
					} catch (NumberFormatException e) {
						thisLine.put(headers.get(i), Double.NaN);
					}
				} else {
					thisLine.put(headers.get(i), Double.NaN);
				}
			}
			// Only return true if we have at least required columns
			return true;
		}
		return false;
	}

	/**
	 * Restart the CSV parser from the beginning (after headers)
	 * 
	 * @throws IOException if the file cannot be reopened
	 */
	public void restart() throws IOException {
		close();
		this.scanner = new Scanner(Path.of(dataPath));
		this.scanner.useDelimiter("\n");
		headersPopulated = false;
		populateHeaders();
		goToNext();
	}

	/**
	 * Get the value for a given header in the current line.
	 * 
	 * @param header column name
	 * @return value as double, or NaN if missing/malformed
	 * @throws IllegalArgumentException if header does not exist
	 */
	public double getValue(String header) {
		if (!headers.contains(header))
			throw new IllegalArgumentException("Header not found: " + header);
		return thisLine.getOrDefault(header, Double.NaN);
	}

	/**
	 * Returns a Bar object for the current line, or null if data is
	 * missing/malformed.
	 * Assumes CSV data is in OHLCV format.
	 */
	public Bar getBar() {
		try {
			if (thisLine.isEmpty() || !thisLine.containsKey("Open"))
				return null;
			double open = getValue("Open");
			double high = getValue("High");
			double low = getValue("Low");
			double close = getValue("Close");
			double volume = getValue("Volume");
			if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)
					|| Double.isNaN(volume))
				return null;
			return new Bar(open, high, low, close, volume);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Close the underlying scanner and release resources.
	 */
	@Override
	public void close() {
		if (scanner != null) {
			scanner.close();
			scanner = null;
		}
	}
}
