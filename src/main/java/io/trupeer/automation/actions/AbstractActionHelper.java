package io.trupeer.automation.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.core.exceptions.FrameworkException;
import io.trupeer.automation.core.locator.LocatorDefinition;
import io.trupeer.automation.core.locator.LocatorRegistry;
import io.trupeer.automation.observability.StructuredLogger;
import io.trupeer.automation.reporting.ScreenshotService;

import java.nio.file.Path;
import java.util.List;

/**
 * Base for the action helpers. resolve() handles primary/fallback retry and returns
 * a ready Locator; logSuccess()/fail() handle logging and the failure screenshot.
 */
public abstract class AbstractActionHelper {

    protected final StructuredLogger log = StructuredLogger.forClass(getClass());
    private static final int FALLBACK_TIMEOUT_MS = 3000;

    protected LocatorDefinition def(String keyOrFriendly) {
        return LocatorRegistry.get().find(keyOrFriendly);
    }

    protected String label(LocatorDefinition def) {
        return def.getFriendlyName() != null ? def.getFriendlyName() : def.getKey();
    }

    /** First selector that attaches wins (primary gets the full timeout, fallbacks a short one). */
    protected Locator resolve(String action, LocatorDefinition def) {
        Page page = DriverManager.getPage();
        List<String> selectors = def.allSelectors();

        for (int i = 0; i < selectors.size(); i++) {
            String selector = selectors.get(i);
            int timeout = (i == 0) ? ConfigurationManager.get().defaultTimeoutMs() : FALLBACK_TIMEOUT_MS;
            try {
                page.locator(selector).first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(timeout));
                return page.locator(selector).first();
            } catch (RuntimeException e) {
                log.action(action, label(def), "RETRY(selector " + (i + 1) + ")", 0, i + 1);
            }
        }
        fail(action, def, new FrameworkException("None of the selectors matched for " + label(def)));
        return null; // unreachable - fail() always throws
    }

    protected void logSuccess(String action, LocatorDefinition def, long startMs) {
        log.action(action, label(def), "SUCCESS", System.currentTimeMillis() - startMs, 1);
    }

    /** Screenshot + throw. */
    protected void fail(String action, LocatorDefinition def, Throwable cause) {
        Path shot = ScreenshotService.capture(action + "_" + def.getKey());
        String message = "[" + action + "] failed on '" + label(def) + "'"
                + (shot != null ? "  (screenshot: " + shot + ")" : "");
        log.error(message, cause);
        throw new FrameworkException(message, cause);
    }
}
