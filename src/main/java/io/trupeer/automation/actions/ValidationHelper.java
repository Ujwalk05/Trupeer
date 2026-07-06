package io.trupeer.automation.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.core.locator.LocatorDefinition;
import org.assertj.core.api.Assertions;

/** Waits, state checks and assertions. Plain try/catch, no lambdas. */
public class ValidationHelper extends AbstractActionHelper {

    // ---- boolean state checks (no wait, no screenshot) ----
    public boolean isDisplayed(LocatorDefinition def) {
        boolean result = DriverManager.getPage().locator(def.getPrimary()).first().isVisible();
        log.action("IS_DISPLAYED", label(def), String.valueOf(result), 0, 1);
        return result;
    }

    public boolean isEnabled(LocatorDefinition def) {
        boolean result = DriverManager.getPage().locator(def.getPrimary()).first().isEnabled();
        log.action("IS_ENABLED", label(def), String.valueOf(result), 0, 1);
        return result;
    }

    public int count(LocatorDefinition def) {
        return DriverManager.getPage().locator(def.getPrimary()).count();
    }

    // ---- waits ----
    /**
     * Waits for the element to be visible; returns true if any of its selectors
     * (primary, then fallbacks) appears within the timeout.
     */
    public boolean waitForVisible(LocatorDefinition def, int timeoutMs) {
        java.util.List<String> selectors = def.allSelectors();
        for (int i = 0; i < selectors.size(); i++) {
            int timeout = (i == 0) ? timeoutMs : 2000;
            try {
                DriverManager.getPage().locator(selectors.get(i)).first()
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
                return true;
            } catch (RuntimeException ignored) {
                // try the next candidate
            }
        }
        return false;
    }

    public void waitForPageLoad() {
        DriverManager.getPage().waitForLoadState(LoadState.LOAD);
    }


    public void assertVisible(LocatorDefinition def, String because) {
        long start = System.currentTimeMillis();
        try {
            resolve("ASSERT_VISIBLE", def).waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));
            Assertions.assertThat(isDisplayed(def)).as(because).isTrue();
            logSuccess("ASSERT_VISIBLE", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("ASSERT_VISIBLE", def, e);
        }
    }

    /** Trimmed visible text of the first match (empty string if none). */
    public String getText(LocatorDefinition def) {
        String text = DriverManager.getPage().locator(def.getPrimary()).first().textContent();
        return text == null ? "" : text.trim();
    }

    // ---- String overloads ----
    public boolean isDisplayed(String key) { return isDisplayed(def(key)); }
    public boolean isEnabled(String key)   { return isEnabled(def(key)); }
    public String getText(String key)      { return getText(def(key)); }
}
