package io.bytegate.log;

public enum LogLevel {
    OFF(0),
    ERROR(1),
    INFO(2),
    DEBUG(3);

    private final int level;

    LogLevel(int level) {
        this.level = level;
    }

    public boolean isEnabled(LogLevel target) {
        return this.level >= target.level;
    }
}
