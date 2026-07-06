package io.trupeer.automation.hooks;

import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.observability.StructuredLogger;
import io.trupeer.automation.pages.Pages;

/**
 * Per-scenario lifecycle. With browser.reuse=true the browser is launched once and
 * reused; @loggedout scenarios wipe the session first.
 */
public class ScenarioHooks {

    private static final StructuredLogger LOG = StructuredLogger.forClass(ScenarioHooks.class);

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        boolean reuse = ConfigurationManager.get().reuseBrowser();
        if (!reuse || !DriverManager.isActive()) {
            DriverManager.startScenario();
        }
        Pages.initialize();
        LOG.info("Starting scenario: " + scenario.getName());
    }

    /** Scenarios tagged @loggedout start from a clean, signed-out session. */
    @Before(value = "@loggedout", order = 10)
    public void freshSession() {
        DriverManager.clearSession();
    }

    @After(order = 0)
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed() && DriverManager.isActive()) {
            byte[] png = DriverManager.getPage()
                    .screenshot(new Page.ScreenshotOptions().setFullPage(true));
            scenario.attach(png, "image/png", scenario.getName());
        }
        // Close per-scenario only when NOT reusing the browser.
        if (!ConfigurationManager.get().reuseBrowser()) {
            DriverManager.teardown();
        }
    }

    /** Runs once after the whole run - close the shared browser. */
    @AfterAll
    public static void afterAll() {
        if (DriverManager.isActive()) {
            DriverManager.teardown();
        }
    }
}
