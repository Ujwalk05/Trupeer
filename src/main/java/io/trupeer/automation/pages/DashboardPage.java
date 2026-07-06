package io.trupeer.automation.pages;

import io.trupeer.automation.core.locator.Loc;
import io.trupeer.automation.core.locator.LocatorDefinition;

/** Catalog of home/dashboard + library locators. */
public final class DashboardPage {

    private DashboardPage() {}

    /** Proof we're logged in ("Welcome back!" is a label; "Create new" is always present). */
    public static final LocatorDefinition WELCOME_BANNER =
            Loc.css("dash.welcome", "Welcome Banner",
                    "label:has-text('Welcome back')", "button:has-text('Create new')");

    /** Sidebar exposes stable ids, e.g. <a id="trupeer-nav-library" href="/library">. */
    public static final LocatorDefinition LIBRARY_NAV =
            Loc.css("dash.libraryNav", "Library Nav", "#trupeer-nav-library", "a[href='/library']");

    /** A video tile links straight to the editor: /content/{id}/video/edit. */
    public static final LocatorDefinition VIDEO_CARD =
            Loc.css("dash.videoCard", "Video Card", "a[href*='/video/edit']");

    /** Account switcher in the sidebar footer (holds the email + chevron icon). */
    public static final LocatorDefinition ACCOUNT_MENU =
            Loc.css("dash.accountMenu", "Account Menu",
                    "[aria-haspopup='menu']:has(svg[class*='chevrons-up-down'])");

    public static final LocatorDefinition LOGOUT =
            Loc.role("dash.logout", "Log out", "role=menuitem[name=\"Log out\"]");
}
