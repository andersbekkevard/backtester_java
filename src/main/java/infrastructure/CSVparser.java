package infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CSVparser {
	private final String dataPath;
	private Scanner scanner;
	private List<String> headers;
	private String[] thisLine;

	public CSVparser(String dataPath) throws IOException {
		this.dataPath = dataPath;
		this.scanner = new Scanner(Path.of(dataPath));
		populateHeaders();
		goToNext();
	}

	private void populateHeaders() {
		if (this.headers != null)
			throw new IllegalStateException();

		String[] headerStrings = scanner.next().split(",");
		this.headers = Arrays.stream(headerStrings).collect(Collectors.toList());
	}

	public boolean goToNext() {
		if (!scanner.hasNext()) {
			return false;
		}

		thisLine = scanner.next().split(",");
		return true;
	}

	public double getValue(String header) {
		if (!headers.contains(header))
			throw new IllegalArgumentException();
		return Double.parseDouble(thisLine[headers.indexOf(header)]);
	}

	public static void main(String[] args) throws IOException {
		CSVparser parser = new CSVparser(StockExchange.AAPL_PATH);
		System.out.println(parser.headers);
		double r = parser.getValue("Return");
		parser.goToNext();
		double nr = parser.getValue("Return");

		System.out.println(r);
		System.out.println(nr);

	}
}
