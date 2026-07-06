package io.trupeer.automation.pages;

import io.trupeer.automation.core.locator.Loc;
import io.trupeer.automation.core.locator.LocatorDefinition;

/** Catalog of login-page locators (/auth?tab=login). No actions - just definitions. */
public final class LoginPage {

    private LoginPage() {}

    public static final LocatorDefinition LOGIN_TAB =
            Loc.role("login.tab", "Login Tab", "role=tab[name=\"Login\"]");

    public static final LocatorDefinition EMAIL =
            Loc.css("login.email", "Email", "input[type='email']", "input[name='email']");

    public static final LocatorDefinition PASSWORD =
            Loc.css("login.password", "Password", "input[type='password']");

    public static final LocatorDefinition SUBMIT =
            Loc.role("login.submit", "Login Button", "role=button[name=\"Login\"]", "button[type='submit']");

    /** Confirmed error text for bad credentials: "Wrong email or password." */
    public static final LocatorDefinition ERROR =
            Loc.text("login.error", "Login Error", "text=/wrong email or password/i");
}
