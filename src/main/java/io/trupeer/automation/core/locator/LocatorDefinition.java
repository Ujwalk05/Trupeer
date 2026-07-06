package io.trupeer.automation.core.locator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * One UI element: a key, a friendly name, a primary selector and an ordered
 * fallback chain. Fallbacks live here rather than in the steps.
 */
public final class LocatorDefinition {

    private final String key;
    private final String friendlyName;
    private final LocatorStrategy strategy;
    private final String primary;
    private final List<String> fallbacks;

    public LocatorDefinition(String key, String friendlyName, LocatorStrategy strategy,
                             String primary, List<String> fallbacks) {
        this.key = key;
        this.friendlyName = friendlyName;
        this.strategy = strategy == null ? LocatorStrategy.CSS : strategy;
        this.primary = primary;
        this.fallbacks = new ArrayList<>(fallbacks == null ? List.of() : fallbacks);
    }

    /** Ordered candidate selectors: primary first, then fallbacks (de-duplicated). */
    public List<String> allSelectors() {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        all.add(primary);
        all.addAll(fallbacks);
        return new ArrayList<>(all);
    }

    public String getKey()             { return key; }
    public String getFriendlyName()    { return friendlyName; }
    public LocatorStrategy getStrategy() { return strategy; }
    public String getPrimary()         { return primary; }
    public List<String> getFallbacks() { return fallbacks; }

    @Override
    public String toString() {
        return "Locator[" + key + " '" + friendlyName + "' primary=" + primary + "]";
    }
}
