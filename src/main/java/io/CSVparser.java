package io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	/**
	 * This formatter can be changed in order to accomodate different csv styles. If
	 * using simple localdateFormatter one has to apply .atStartOfDay() to get
	 * LocalDateTime
	 */

	private static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

	private final String dataPath;
	private Scanner scanner;
	private List<String> headers;
	private LocalDateTime thisTimestamp;
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
	 * Advances to the next data line, parsing date into thisTimestamp and values
	 * into thisLine map.
	 * Handles missing or malformed data gracefully.
	 * 
	 * @return true if a new line was read, false if EOF
	 */
	public boolean goToNext() {
		thisLine.clear();
		while (scanner.hasNext()) {
			String line = scanner.next();
			// Skipping blank lines
			if (line.trim().isEmpty())
				continue;

			String[] tokens = line.split(",");
			thisTimestamp = LocalDate.parse(tokens[0], localDateFormatter).atStartOfDay();
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

	public double getDoubleValue(String header) {
		if (!headers.contains(header) || "Date".equals(header))
			throw new IllegalArgumentException("Header not found: " + header);
		return thisLine.getOrDefault(header, Double.NaN);
	}

	public LocalDateTime getTimestamp() {
		return this.thisTimestamp;
	}

	/**
	 * Returns a Bar object for the current line, or null if data is
	 * missing/malformed.
	 * Assumes CSV data is in "Date,O,H,L,C,V\n" format.
	 */
	public Bar getBar() {
		try {
			if (thisLine.isEmpty() || !thisLine.containsKey("Open"))
				return null;
			LocalDateTime timestamp = getTimestamp();
			double open = getDoubleValue("Open");
			double high = getDoubleValue("High");
			double low = getDoubleValue("Low");
			double close = getDoubleValue("Close");
			double volume = getDoubleValue("Volume");
			if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)
					|| Double.isNaN(volume))
				return null;
			return new Bar(timestamp, open, high, low, close, volume);
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
