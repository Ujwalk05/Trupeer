package io.trupeer.automation.pages;

import io.trupeer.automation.core.locator.Loc;
import io.trupeer.automation.core.locator.LocatorDefinition;

/** Catalog of video-editor locators (/content/{id}/video/edit). */
public final class EditorPage {

    private EditorPage() {}

    // ---- key regions ----
    public static final LocatorDefinition SCRIPT_PANEL =
            Loc.xpath("editor.scriptPanel", "Script Panel", "//div[@class='editVideoBar_editAreaBodyScript__1j1cW']", "[data-slate-editor='true']");

    /** Each timestamped script paragraph is a Slate editor block. */
    public static final LocatorDefinition SCRIPT_BLOCK =
            Loc.xpath("editor.scriptBlock", "Script Block", "//div[@class='editVideoBar_editAreaBodyScript__1j1cW']");

    public static final LocatorDefinition TIMELINE =
            Loc.xpath("editor.timeline", "Timeline",
                    "//div[@class='flex items-end relative h-full w-full']", "text=/Add Scenes/i");

    // Preview is a WebGL player: <div class="WebGLPlayer_webglPlayerContainer__...">.
    // Match by class prefix so the CSS-module hash / opacity classes don't matter.
    public static final LocatorDefinition PREVIEW =
            Loc.css("editor.preview", "Preview", "[class*='webglPlayerContainer']", "canvas");

    // ---- Modify Script with AI ----
    /** The three icon actions above the script; the wand opens the rewrite dialog. */
    public static final LocatorDefinition SCRIPT_TOOLBAR_BUTTON =
            Loc.css("editor.toolbarButton", "Script Toolbar Button", "[class*='editVideoBar_editActions'] button");

    public static final LocatorDefinition AI_DIALOG_TITLE =
            Loc.text("editor.aiDialog", "Add Custom Instructions", "text=Add Custom Instructions");

    public static final LocatorDefinition AI_PROMPT =
            Loc.css("editor.aiPrompt", "AI Prompt", "textarea[maxlength='300']", "textarea[placeholder*='conversational' i]");

    public static final LocatorDefinition REWRITE_BUTTON =
            Loc.role("editor.rewrite", "Rewrite Script", "role=button[name=\"Rewrite Script\"]");

    // After a rewrite the editor swaps the toolbar for these review controls -
    // their appearance confirms a modified script was returned and displayed.
    public static final LocatorDefinition KEEP_CHANGES =
            Loc.css("editor.keepChanges", "Keep changes", "button:has-text('Keep changes')");

    public static final LocatorDefinition DISCARD_CHANGES =
            Loc.css("editor.discardChanges", "Discard changes", "button:has-text('Discard changes')");

    // ---- Visuals -> Background (editor interaction) ----
    // Editor tabs are <button class="textbtn ...">Visuals</button>.
    public static final LocatorDefinition VISUALS_TAB =
            Loc.xpath("editor.visualsTab", "Visuals Tab", "//button[.='Visuals']");

    public static final LocatorDefinition BACKGROUND_SUBTAB =
            Loc.xpath("editor.backgroundSubTab", "Background Sub Tab",
                    "//button[.='Background']", "text=/^Background$/");

    /** Background swatches are a HeadlessUI radio group. */
    public static final LocatorDefinition BACKGROUND_OPTION =
            Loc.css("editor.bgOption", "Background Option", "[role='radio']");

    // The selected swatch is the radio that renders the checkmark <svg> child.
    public static final LocatorDefinition BACKGROUND_SELECTED =
            Loc.css("editor.bgSelected", "Selected Background", "[role='radio']:has(svg)");
}
