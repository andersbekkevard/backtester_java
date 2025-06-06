package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import engine.Bar;

public class CSVparserTest {
	private Path tempFile;

	@Before
	public void setUp() throws IOException {
		tempFile = Files.createTempFile("test", ".csv");
	}

	@After
	public void tearDown() throws IOException {
		Files.deleteIfExists(tempFile);
	}

	@Test
	public void testParseHeadersAndValues() throws IOException {
		String csv = "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105,1000\n";
		Files.write(tempFile, csv.getBytes());
		try (CSVparser parser = new CSVparser(tempFile.toString())) {
			assertEquals(100.0, parser.getDoubleValue("Open"), 0.0001);
			assertEquals(110.0, parser.getDoubleValue("High"), 0.0001);
			assertEquals(90.0, parser.getDoubleValue("Low"), 0.0001);
			assertEquals(105.0, parser.getDoubleValue("Close"), 0.0001);
			assertEquals(1000.0, parser.getDoubleValue("Volume"), 0.0001);
		}
	}

	@Test
	public void testGoToNext() throws IOException {
		String csv = "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105,1000\n2021-01-02,101,111,91,106,1100\n";
		Files.write(tempFile, csv.getBytes());
		try (CSVparser parser = new CSVparser(tempFile.toString())) {
			assertEquals(100.0, parser.getDoubleValue("Open"), 0.0001);
			assertTrue(parser.goToNext());
			assertEquals(101.0, parser.getDoubleValue("Open"), 0.0001);
			assertFalse(parser.goToNext());
		}
	}

	@Test
	public void testMalformedLine() throws IOException {
		String csv = "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105\n"; // Volume missing
		Files.write(tempFile, csv.getBytes());
		try (CSVparser parser = new CSVparser(tempFile.toString())) {
			assertTrue(Double.isNaN(parser.getDoubleValue("Volume")));
		}
	}

	@Test
	public void testGetBar() throws IOException {
		String csv = "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105,1000\n";
		Files.write(tempFile, csv.getBytes());
		try (CSVparser parser = new CSVparser(tempFile.toString())) {
			Bar bar = parser.getBar();
			assertNotNull(bar);
			assertEquals(100.0, bar.open(), 0.0001);
			assertEquals(110.0, bar.high(), 0.0001);
			assertEquals(90.0, bar.low(), 0.0001);
			assertEquals(105.0, bar.close(), 0.0001);
			assertEquals(1000.0, bar.volume(), 0.0001);
		}
	}

	@Test
	public void testRestart() throws IOException {
		String csv = "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105,1000\n2021-01-02,101,111,91,106,1100\n";
		Files.write(tempFile, csv.getBytes());
		try (CSVparser parser = new CSVparser(tempFile.toString())) {
			parser.goToNext(); // Move to second line
			assertEquals(101.0, parser.getDoubleValue("Open"), 0.0001);
			parser.restart();
			assertEquals(100.0, parser.getDoubleValue("Open"), 0.0001);
		}
	}

	@Test
	public void testEmptyFileThrows() throws IOException {
		Files.write(tempFile, "".getBytes());
		try {
			CSVparser parser = new CSVparser(tempFile.toString());
			fail("Expected IllegalStateException");
		} catch (Exception e) {
			// expected
		}
	}

	@Test
	public void testHeaderValidation() throws IOException {
		String csv = "Open\n100\n"; // Only one header
		Files.write(tempFile, csv.getBytes());
		try {
			CSVparser parser = new CSVparser(tempFile.toString());
			fail("Expected IllegalArgumentException");
		} catch (Exception e) {
			// expected
		}
	}
}
