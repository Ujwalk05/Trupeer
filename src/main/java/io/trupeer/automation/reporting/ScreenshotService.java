package io.trupeer.automation.reporting;

import com.microsoft.playwright.Page;
import io.trupeer.automation.core.config.FrameworkConstants;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.observability.StructuredLogger;

import java.nio.file.Files;
import java.nio.file.Path;

/** Captures a full-page screenshot into the run's screenshots folder. */
public final class ScreenshotService {

    private static final StructuredLogger LOG = StructuredLogger.forClass(ScreenshotService.class);

    private ScreenshotService() {}

    /** Returns the saved path, or null if capture failed (never masks the original error). */
    public static Path capture(String name) {
        try {
            Path dir = FrameworkConstants.screenshotsDir();
            Files.createDirectories(dir);
            Path file = dir.resolve(sanitize(name) + "_" + System.currentTimeMillis() + ".png");
            DriverManager.getPage().screenshot(
                    new Page.ScreenshotOptions().setPath(file).setFullPage(true));
            return file;
        } catch (Exception e) {
            LOG.error("Failed to capture screenshot for '" + name + "'", e);
            return null;
        }
    }

    private static String sanitize(String s) {
        return s == null ? "screenshot" : s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
