package io.trupeer.automation.core.locator;

import io.trupeer.automation.core.exceptions.FrameworkException;
import io.trupeer.automation.core.exceptions.LocatorNotFoundException;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central index of every LocatorDefinition, populated as Page classes load (each
 * Loc.define(...) self-registers here). Lets a step resolve an element by its
 * stable key or its friendly name.
 */
public final class LocatorRegistry {

    private static final LocatorRegistry INSTANCE = new LocatorRegistry();

    private final Map<String, LocatorDefinition> byKey = new ConcurrentHashMap<>();
    private final Map<String, LocatorDefinition> byFriendly = new ConcurrentHashMap<>();

    private LocatorRegistry() {}

    public static LocatorRegistry get() { return INSTANCE; }

    public void register(LocatorDefinition def) {
        if (byKey.putIfAbsent(def.getKey(), def) != null) {
            throw new FrameworkException("Duplicate locator key: '" + def.getKey() + "'");
        }
        String friendly = def.getFriendlyName();
        if (friendly != null && !friendly.isBlank()) {
            byFriendly.putIfAbsent(friendly.toLowerCase(Locale.ROOT).trim(), def);
        }
    }

    /** Resolve by key first, then by friendly name (case-insensitive). */
    public LocatorDefinition find(String keyOrFriendlyName) {
        if (keyOrFriendlyName == null) throw new LocatorNotFoundException("null");
        LocatorDefinition def = byKey.get(keyOrFriendlyName);
        if (def == null) {
            def = byFriendly.get(keyOrFriendlyName.toLowerCase(Locale.ROOT).trim());
        }
        if (def == null) throw new LocatorNotFoundException(keyOrFriendlyName);
        return def;
    }

    public int size() { return byKey.size(); }
}
