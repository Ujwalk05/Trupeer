package io.trupeer.automation.observability;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Log4j2 wrapper that logs each UI action in a consistent format. */
public final class StructuredLogger {

    private final Logger logger;

    private StructuredLogger(Class<?> clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    public static StructuredLogger forClass(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }

    /** e.g. [CLICK] Login -> SUCCESS (120ms, attempt 1) */
    public void action(String action, String locator, String status, long durationMs, int attempt) {
        logger.info("[{}] {} -> {} ({}ms, attempt {})", action, locator, status, durationMs, attempt);
    }

    public void info(String message)               { logger.info(message); }
    public void debug(String message)              { logger.debug(message); }
    public void warn(String message)               { logger.warn(message); }
    public void error(String message, Throwable t) { logger.error(message, t); }
}
