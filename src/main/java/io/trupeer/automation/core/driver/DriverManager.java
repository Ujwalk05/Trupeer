package io.trupeer.automation.core.driver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.exceptions.FrameworkException;

/**
 * Thread-local holder for the Playwright objects. With browser.reuse=true the run
 * shares one Browser/Context/Page so a login persists across scenarios; @loggedout
 * scenarios call {@link #clearSession()} first.
 */
public final class DriverManager {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private DriverManager() {}

    public static void startScenario() {
        ConfigurationManager cfg = ConfigurationManager.get();

        Playwright playwright = PlaywrightFactory.createPlaywright();
        Browser browser = PlaywrightFactory.launchBrowser(playwright);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        if (cfg.headless()) {
            contextOptions.setViewportSize(1440, 900);  // deterministic size for headless/CI
        } else {
            contextOptions.setViewportSize(null);       // null = use the maximized window
        }
        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(cfg.defaultTimeoutMs());

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(context.newPage());
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new FrameworkException("No Page bound to thread '"
                    + Thread.currentThread().getName() + "' - was startScenario() called?");
        }
        return page;
    }

    /** True if a browser/page is already bound to this thread (reuse mode). */
    public static boolean isActive() { return PAGE.get() != null; }

    /** Clears cookies + local/session storage so the next steps start signed-out. */
    public static void clearSession() {
        if (CONTEXT.get() == null) return;
        CONTEXT.get().clearCookies();
        try {
            getPage().navigate(ConfigurationManager.get().baseUrl());
            getPage().evaluate("() => { try { localStorage.clear(); sessionStorage.clear(); } catch (e) {} }");
        } catch (Exception ignored) {
            // best effort
        }
    }

    /** Closes everything and clears every ThreadLocal. */
    public static void teardown() {
        try {
            if (CONTEXT.get() != null) CONTEXT.get().close();
            if (BROWSER.get() != null) BROWSER.get().close();
            if (PLAYWRIGHT.get() != null) PLAYWRIGHT.get().close();
        } finally {
            PLAYWRIGHT.remove();
            BROWSER.remove();
            CONTEXT.remove();
            PAGE.remove();
        }
    }
}
