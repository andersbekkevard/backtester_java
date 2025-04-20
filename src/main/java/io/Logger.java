package io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public void infoNoFlag(String msg) {
        writer.println(msg);
    }

    private final PrintWriter writer;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean showDateTime = false;

    public Logger(OutputStream out) {
        this.writer = new PrintWriter(out, true);
    }

    public void setShowDateTime(boolean show) {
        this.showDateTime = show;
    }

    public void info(String msg) {
        if (showDateTime) {
            writer.printf("%s [INFO] %s%n", LocalDateTime.now().format(dtf), msg);
        } else {
            writer.printf("[INFO] %s%n", msg);
        }
    }

    public void error(String msg) {
        if (showDateTime) {
            writer.printf("%s [ERROR] %s%n", LocalDateTime.now().format(dtf), msg);
        } else {
            writer.printf("[ERROR] %s%n", msg);
        }
    }

    public void error(String msg, Throwable t) {
        if (showDateTime) {
            writer.printf("%s [ERROR] %s - %s%n", LocalDateTime.now().format(dtf), msg, t.toString());
        } else {
            writer.printf("[ERROR] %s - %s%n", msg, t.toString());
        }
        t.printStackTrace(writer);
        writer.flush();
    }
}
