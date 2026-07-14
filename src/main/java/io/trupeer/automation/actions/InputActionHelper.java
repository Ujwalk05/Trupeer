package io.trupeer.automation.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.locator.LocatorDefinition;

import java.util.Objects;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Text-input interactions. Plain try/catch, no lambdas. */
public class InputActionHelper extends AbstractActionHelper {

    public void fill(LocatorDefinition def, String value) {
        Objects.requireNonNull(value, "fill() value must not be null for " + label(def));
        long start = System.currentTimeMillis();
        try {
            readyForInput("FILL", def).fill(value);
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
            Locator field = readyForInput("FILL", def);
            for (int attempt = 0; attempt < 3; attempt++) {
                field.fill(value);
                try {
                    assertValue(field, value);
                    logSuccess("FILL", def, start);
                    return;
                } catch (RuntimeException | AssertionError ignored) {
                    field = readyForInput("FILL_RETRY", def);
                }
            }
            assertValue(field, value);
            logSuccess("FILL", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("FILL", def, e);
        }
    }

    public void assertValue(LocatorDefinition def, String value) {
        Objects.requireNonNull(value, "assertValue() value must not be null for " + label(def));
        long start = System.currentTimeMillis();
        try {
            assertValue(readyForInput("ASSERT_VALUE", def), value);
            logSuccess("ASSERT_VALUE", def, start);
        } catch (RuntimeException | AssertionError e) {
            fail("ASSERT_VALUE", def, e);
        }
    }

    /** Returns the current value of an input/textarea (bounded by its own maxlength). */
    public String valueOf(LocatorDefinition def) {
        return readyForInput("VALUE_OF", def).inputValue();
    }

    // ---- String overloads ----
    public void fill(String key, String value) { fill(def(key), value); }
    public String valueOf(String key)          { return valueOf(def(key)); }

    private Locator readyForInput(String action, LocatorDefinition def) {
        Locator field = resolve(action, def);
        int timeout = ConfigurationManager.get().defaultTimeoutMs();
        field.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeout));
        assertThat(field).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(timeout));
        assertThat(field).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(timeout));
        assertThat(field).isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(timeout));
        return field;
    }

    private void assertValue(Locator field, String value) {
        assertThat(field).hasValue(value,
                new LocatorAssertions.HasValueOptions()
                        .setTimeout(ConfigurationManager.get().defaultTimeoutMs()));
    }
}