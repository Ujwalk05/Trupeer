package io.trupeer.automation.core.exceptions;

/**
 * Thrown when a logical locator key (or friendly name) is not registered in the
 * LocatorRegistry - i.e. a step referenced an element that no Page class defines.
 */
public class LocatorNotFoundException extends FrameworkException {

    public LocatorNotFoundException(String keyName) {
        super("No locator registered for: '" + keyName
                + "'. Check the Page classes under io.trupeer.automation.pages "
                + "(every locator is defined there via Loc.define(...)).");
    }
}
