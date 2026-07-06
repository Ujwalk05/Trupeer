package io.trupeer.automation.core.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.exceptions.FrameworkException;

import java.util.List;

/**
 * Builds Playwright + Browser from configuration. The BrowserContext and Page are
 * created per scenario inside DriverManager, so this class only owns browser launch.
 */
public final class PlaywrightFactory {

    private PlaywrightFactory() {}

    public static Playwright createPlaywright() {
        return Playwright.create();
    }

    public static Browser launchBrowser(Playwright playwright) {
        ConfigurationManager cfg = ConfigurationManager.get();
        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(cfg.headless());
        if (!cfg.headless()) {
            opts.setArgs(List.of("--start-maximized"));
        }

        switch (cfg.browser().toLowerCase()) {
            case "chromium":
            case "chrome":
                return playwright.chromium().launch(opts);
            case "firefox":
                return playwright.firefox().launch(opts);
            case "webkit":
            case "safari":
                return playwright.webkit().launch(opts);
            default:
                throw new FrameworkException("Unsupported browser: '" + cfg.browser()
                        + "'. Use chromium | firefox | webkit.");
        }
    }
}
