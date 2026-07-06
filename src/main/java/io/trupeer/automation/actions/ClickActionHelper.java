package io.trupeer.automation.actions;

import io.trupeer.automation.core.locator.LocatorDefinition;

/** Click-family interactions. Plain try/catch, no lambdas. */
public class ClickActionHelper extends AbstractActionHelper {

    public void click(LocatorDefinition def) {
        long start = System.currentTimeMillis();
        try {
            resolve("CLICK", def).click();
            logSuccess("CLICK", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("CLICK", def, e);
        }
    }

    public void hover(LocatorDefinition def) {
        long start = System.currentTimeMillis();
        try {
            resolve("HOVER", def).hover();
            logSuccess("HOVER", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("HOVER", def, e);
        }
    }

    // ---- String overloads (resolve by key / friendly name) ----
    public void click(String key) { click(def(key)); }
    public void hover(String key) { hover(def(key)); }
}
