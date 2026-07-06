package io.trupeer.automation.actions;

import io.trupeer.automation.core.locator.LocatorDefinition;

import java.util.Objects;

/** Text-input interactions. Plain try/catch, no lambdas. */
public class InputActionHelper extends AbstractActionHelper {

    public void fill(LocatorDefinition def, String value) {
        Objects.requireNonNull(value, "fill() value must not be null for " + label(def));
        long start = System.currentTimeMillis();
        try {
            resolve("FILL", def).fill(value);
            logSuccess("FILL", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("FILL", def, e);
        }
    }

    /**
     * Fills a field and confirms the value stuck, re-filling if a React re-render
     * cleared it (common on SPA login forms just after a redirect/hydration).
     */
    public void fillVerified(LocatorDefinition def, String value) {
        Objects.requireNonNull(value, "fillVerified() value must not be null for " + label(def));
        long start = System.currentTimeMillis();
        try {
            com.microsoft.playwright.Locator field = resolve("FILL", def);
            field.fill(value);
            for (int attempt = 0; attempt < 3 && !value.equals(field.inputValue()); attempt++) {
                field.fill(value);
            }
            logSuccess("FILL", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("FILL", def, e);
        }
    }

    /** Returns the current value of an input/textarea (bounded by its own maxlength). */
    public String valueOf(LocatorDefinition def) {
        return resolve("VALUE_OF", def).inputValue();
    }

    // ---- String overloads ----
    public void fill(String key, String value) { fill(def(key), value); }
    public String valueOf(String key)          { return valueOf(def(key)); }
}
