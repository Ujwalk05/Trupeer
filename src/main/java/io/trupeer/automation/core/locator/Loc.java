package io.trupeer.automation.core.locator;

import java.util.List;

/** Factory used by the Page classes. Building a locator also registers it. */
public final class Loc {

    private Loc() {}

    public static LocatorDefinition css(String key, String friendlyName,
                                        String primary, String... fallbacks) {
        return define(key, friendlyName, LocatorStrategy.CSS, primary, fallbacks);
    }

    public static LocatorDefinition xpath(String key, String friendlyName,
                                          String primary, String... fallbacks) {
        return define(key, friendlyName, LocatorStrategy.XPATH, primary, fallbacks);
    }

    /** Playwright text= selector, e.g. text=/wrong email or password/i */
    public static LocatorDefinition text(String key, String friendlyName,
                                         String primary, String... fallbacks) {
        return define(key, friendlyName, LocatorStrategy.TEXT, primary, fallbacks);
    }

    /** Playwright role= selector, e.g. role=button[name="Login"] */
    public static LocatorDefinition role(String key, String friendlyName,
                                         String primary, String... fallbacks) {
        return define(key, friendlyName, LocatorStrategy.ROLE, primary, fallbacks);
    }

    public static LocatorDefinition define(String key, String friendlyName, LocatorStrategy strategy,
                                           String primary, String... fallbacks) {
        LocatorDefinition def = new LocatorDefinition(
                key, friendlyName, strategy, primary, List.of(fallbacks));
        LocatorRegistry.get().register(def);
        return def;
    }
}
