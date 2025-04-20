package engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import accounts.Portfolio;
import io.Logger;

public class StockExchangeTest {
    private StockExchange exchange;
    private Logger logger;
    private Path csvFile;

    @Before
    public void setUp() throws IOException {
        logger = new Logger(System.out);
        csvFile = Files.createTempFile("stocktest", ".csv");
        // Write a simple CSV file for testing
        Files.write(csvFile,
                "Date,Open,High,Low,Close,Volume\n2021-01-01,100,110,90,105,1000\n2021-01-02,101,111,91,106,1100\n"
                        .getBytes());
        exchange = new StockExchange(logger);
        exchange.addStock("AAPL", csvFile.toString());

    }

    @Test
    public void testAddPortfolioAndStep() {
        Portfolio portfolio = new Portfolio(1000.0, logger);
        exchange.addPortfolio(portfolio);
        // Should not throw and should call acceptBars at least once
        boolean stepped = exchange.step();
        assertTrue(stepped);
    }

    @Test
    public void testParserEndHandling() {
        // Step until the parser is finished
        boolean anyLeft = true;
        int steps = 0;
        while (anyLeft && steps < 10) {
            anyLeft = exchange.step();
            steps++;
        }
        // After enough steps, step() should return false
        assertFalse(anyLeft);
    }
}
