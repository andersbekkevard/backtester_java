package io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class LoggerTest {
    private ByteArrayOutputStream outContent;
    private Logger logger;

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        logger = new Logger(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        outContent = null;
        logger = null;
    }

    @Test
    public void testInfoNoFlag() {
        logger.infoNoFlag("Hello");
        String output = outContent.toString();
        assertTrue(output.contains("Hello"));
    }

    @Test
    public void testInfo() {
        logger.info("Test info");
        String output = outContent.toString();
        assertTrue(output.contains("INFO"));
        assertTrue(output.contains("Test info"));
    }

    @Test
    public void testError() {
        logger.error("Test error");
        String output = outContent.toString();
        assertTrue(output.contains("ERROR"));
        assertTrue(output.contains("Test error"));
    }

    @Test
    public void testErrorWithException() {
        Exception ex = new Exception("fail");
        logger.error("msg", ex);
        String output = outContent.toString();
        assertTrue(output.contains("ERROR"));
        assertTrue(output.contains("msg"));
        assertTrue(output.contains("fail"));
    }
}
