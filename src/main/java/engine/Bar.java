package engine;

import java.time.LocalDateTime;

public record Bar(LocalDateTime timestamp, double open, double high, double low, double close, double volume) {
}