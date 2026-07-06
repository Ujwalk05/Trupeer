package io.trupeer.automation.pages;

import io.trupeer.automation.core.exceptions.FrameworkException;

/**
 * Loads every Page class once at startup so their Loc.* fields self-register in the
 * LocatorRegistry. When you add a new Page class, add one line in initialize().
 */
public final class Pages {

    private static volatile boolean initialized = false;

    private Pages() {}

    public static synchronized void initialize() {
        if (initialized) return;
        force(LoginPage.class);
        force(DashboardPage.class);
        force(EditorPage.class);
        initialized = true;
    }

    private static void force(Class<?> pageClass) {
        try {
            Class.forName(pageClass.getName());   // triggers static field registration
        } catch (ClassNotFoundException e) {
            throw new FrameworkException("Cannot load page class: " + pageClass, e);
        }
    }
}
