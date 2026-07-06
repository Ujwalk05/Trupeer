package io.trupeer.automation.core.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable, run-wide constants. RUN_ID is generated once per JVM launch and used
 * to keep each run's reports and screenshots in their own folder.
 */
public final class FrameworkConstants {

    private FrameworkConstants() {}

    /** Unique id for this execution, e.g. run_20260703_142233 */
    public static final String RUN_ID =
            "run_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    private static final Path TARGET = Paths.get(System.getProperty("user.dir"), "target");

    public static Path reportsDir()     { return TARGET.resolve("reports"); }
    public static Path screenshotsDir() { return TARGET.resolve("screenshots").resolve(RUN_ID); }
}
