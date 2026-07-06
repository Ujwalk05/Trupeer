package io.trupeer.automation.core.locator;

/** Selector engine hint. page.locator() auto-detects css / //xpath / text= / role=. */
public enum LocatorStrategy {
    CSS,
    XPATH,
    TEXT,
    ROLE
}
