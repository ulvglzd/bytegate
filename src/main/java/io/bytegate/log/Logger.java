package io.bytegate.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final LogLevel logLevel;

    public Logger(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    private void log(LogLevel target, String message) {
        if (logLevel.isEnabled(target)) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] [%s] [%s] %s%n", timestamp, target, threadName, message);
        }
    }
}
