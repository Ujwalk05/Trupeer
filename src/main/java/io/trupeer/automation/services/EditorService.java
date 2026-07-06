package io.trupeer.automation.services;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.trupeer.automation.actions.InputActionHelper;
import io.trupeer.automation.actions.ValidationHelper;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.core.exceptions.FrameworkException;
import io.trupeer.automation.pages.DashboardPage;
import io.trupeer.automation.pages.EditorPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Editor operations shared by the Cucumber steps and the Part 3 validator:
 * opening a video, and driving the "Modify Script with AI" (Rewrite) flow.
 */
public class EditorService {

    private static final String SLATE_BLOCK = "[data-slate-editor='true']";
    private static final String TOOLBAR_BUTTONS = "[class*='editVideoBar_editActions'] button";

    private final ConfigurationManager cfg = ConfigurationManager.get();
    private final InputActionHelper input = new InputActionHelper();
    private final ValidationHelper validate = new ValidationHelper();

    private Page page() {
        return DriverManager.getPage();
    }

    // ---- navigation ----

    public boolean isInEditor() {
        return page().url().contains("/video/edit");
    }

    /** Opens the first video in the library and lands on its editor page. */
    public void openFirstVideo() {
        Page page = page();
        page.navigate(cfg.baseUrl() + "/library");
        page.waitForURL("**/library**");
        if (!validate.waitForVisible(DashboardPage.VIDEO_CARD, 10000)) {
            throw new FrameworkException("No existing video found - Part 1 should have created one.");
        }
        page.locator(DashboardPage.VIDEO_CARD.getPrimary()).first().click();
        page.waitForURL("**/video/edit**");
    }

    public boolean editorLoaded() {
        return validate.waitForVisible(EditorPage.TIMELINE, 15000)
                && validate.waitForVisible(EditorPage.PREVIEW, 15000)
                && validate.waitForVisible(EditorPage.SCRIPT_PANEL, 15000);
    }

    // ---- script + Modify Script with AI ----

    /** Concatenated text of every Slate script block currently shown. */
    public String currentScript() {
        List<String> blocks = page().locator(SLATE_BLOCK).allInnerTexts();
        return String.join("\n", blocks).trim();
    }

    /**
     * If the editor is in the post-rewrite "Keep changes / Discard changes" state,
     * discard it - reverts the script and restores the normal toolbar (with the wand).
     */
    public void discardPendingRewrite() {
        Locator discard = page().locator(EditorPage.DISCARD_CHANGES.getPrimary());
        if (discard.count() > 0 && discard.first().isVisible()) {
            discard.first().click();
            try {
                discard.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
            } catch (RuntimeException ignored) {
                // already gone
            }
        }
    }

    /**
     * Opens the "Add Custom Instructions" dialog via the wand (tooltip "Rewrite
     * script again with AI"). Identified by tooltip so we never trigger a different
     * AI action; falls back to the middle icon if the tooltip is slow.
     */
    public void openModifyWithAiDialog() {
        List<Locator> iconButtons = waitForIconButtons();
        for (Locator button : iconButtons) {
            button.hover();
            String tooltip = readTooltip();
            if (tooltip.contains("rewrite") && tooltip.contains("again")) {
                button.click();
                return;
            }
        }
        if (iconButtons.size() >= 2) {   // sparkle, wand, magnifier -> wand is the middle icon
            iconButtons.get(1).click();
            return;
        }
        throw new FrameworkException("Could not open the 'Rewrite script again with AI' dialog.");
    }

    public boolean isDialogOpen() {
        return validate.waitForVisible(EditorPage.AI_PROMPT, 10000);
    }

    public void enterPrompt(String prompt) {
        input.fill(EditorPage.AI_PROMPT, prompt);
    }

    public int promptLength() {
        return input.valueOf(EditorPage.AI_PROMPT).length();
    }

    public boolean isRewriteEnabled() {
        return validate.isEnabled(EditorPage.REWRITE_BUTTON);
    }

    public void clickRewrite() {
        page().locator(EditorPage.REWRITE_BUTTON.getPrimary()).first().click();
    }

    public boolean reviewControlsVisible() {
        return validate.waitForVisible(EditorPage.KEEP_CHANGES, 10000);
    }

    /** Waits until the shown script differs from {@code original} (the AI result rendered). */
    public boolean waitForScriptToChange(String original, int timeoutMs) {
        try {
            page().waitForFunction(
                    "(prev) => {"
                            + " const els = document.querySelectorAll(\"[data-slate-editor='true']\");"
                            + " const text = Array.from(els).map(e => e.innerText).join('\\n').trim();"
                            + " return text.length > 0 && text !== prev; }",
                    original,
                    new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * High-level helper for Part 3: send one prompt through Modify Script with AI
     * and return the rewritten script, then revert so the base script is unchanged
     * for the next prompt. Empty result means the rewrite did not produce a change.
     */
    public Optional<String> rewriteWithPrompt(String prompt, int timeoutMs) {
        discardPendingRewrite();
        String before = currentScript();

        openModifyWithAiDialog();
        isDialogOpen();
        enterPrompt(prompt);
        page().waitForTimeout(2000); 
        clickRewrite();

        boolean changed = waitForScriptToChange(before, timeoutMs);
        String after = currentScript();
        page().waitForTimeout(2000);  
        discardPendingRewrite();

        return changed ? Optional.of(after) : Optional.empty();
    }

    // ---- internals ----

    private List<Locator> waitForIconButtons() {
        long deadline = System.currentTimeMillis() + 15000;
        List<Locator> iconButtons = new ArrayList<>();
        while (System.currentTimeMillis() < deadline) {
            iconButtons.clear();
            for (Locator button : page().locator(TOOLBAR_BUTTONS).all()) {
                try {
                    if (button.innerText().trim().isEmpty()) {   // icon-only (skip "+ Add")
                        iconButtons.add(button);
                    }
                } catch (RuntimeException ignored) {
                    // re-rendering mid-read; retry next poll
                }
            }
            if (iconButtons.size() >= 2) {
                return iconButtons;
            }
            page().waitForTimeout(500);
        }
        return iconButtons;
    }

    private String readTooltip() {
        Locator tip = page().locator("[data-radix-popper-content-wrapper], [role='tooltip']");
        try {
            tip.first().waitFor(new Locator.WaitForOptions().setTimeout(1500));
            return tip.first().innerText().toLowerCase();
        } catch (RuntimeException e) {
            return "";
        }
    }
}
