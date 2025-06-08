package accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PortfolioHistoryTest {
    private PortfolioHistory history;
    private Path tempFile;

    @Before
    public void setUp() throws IOException {
        history = new PortfolioHistory();
        tempFile = Files.createTempFile("history", ".csv");
        history.record(LocalDateTime.of(2023,1,1,0,0), Collections.emptyMap(), 100.0);
        history.record(LocalDateTime.of(2023,1,2,0,0), Collections.emptyMap(), 110.0);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testSaveToCsv() throws IOException {
        history.saveToCsv(tempFile);
        assertTrue(Files.exists(tempFile));
        java.util.List<String> lines = Files.readAllLines(tempFile);
        assertEquals(3, lines.size());
        assertTrue(lines.get(1).contains("2023-01-01"));
        assertTrue(lines.get(2).contains("110.0"));
    }
}
