package io.trupeer.automation.stepdefinitions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.trupeer.automation.actions.ClickActionHelper;
import io.trupeer.automation.actions.ValidationHelper;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.core.exceptions.FrameworkException;
import io.trupeer.automation.pages.EditorPage;
import io.trupeer.automation.services.EditorService;
import org.assertj.core.api.Assertions;

/** Opening a video in the editor and the background-change interaction. */
public class EditorSteps {

    private static final String RADIO = "[role='radio']";

    private final EditorService editor;
    private final ClickActionHelper click;
    private final ValidationHelper validate;

    private String previousBackgroundId;
    private String chosenBackgroundId;

    public EditorSteps(EditorService editor, ClickActionHelper click, ValidationHelper validate) {
        this.editor = editor;
        this.click = click;
        this.validate = validate;
    }

    @When("the user opens an existing video in the editor")
    public void theUserOpensAnExistingVideoInTheEditor() {
        if (editor.isInEditor()) {
            return;
        }
        editor.openFirstVideo();
        Assertions.assertThat(DriverManager.getPage().url())
                .as("Should navigate to the editor after opening a video")
                .contains("/video/edit");
    }

    @Then("the editor page loads with its key elements")
    public void theEditorPageLoadsWithItsKeyElements() {
        Assertions.assertThat(validate.waitForVisible(EditorPage.TIMELINE, 15000))
                .as("Editor timeline should be visible").isTrue();
        Assertions.assertThat(validate.waitForVisible(EditorPage.PREVIEW, 15000))
                .as("Editor preview/player should be visible").isTrue();
        Assertions.assertThat(validate.waitForVisible(EditorPage.SCRIPT_PANEL, 15000))
                .as("Editor script panel should be visible").isTrue();
    }

    @When("the user changes the video background")
    public void theUserChangesTheVideoBackground() {
        click.click(EditorPage.VISUALS_TAB);
        click.click(EditorPage.BACKGROUND_SUBTAB);
        Assertions.assertThat(validate.waitForVisible(EditorPage.BACKGROUND_OPTION, 10000))
                .as("Background swatches should render").isTrue();

        Page page = DriverManager.getPage();
        previousBackgroundId = selectedBackgroundId(page);
        chosenBackgroundId = selectDifferentBackground(page, previousBackgroundId);
    }

    @Then("the selected background is updated")
    public void theSelectedBackgroundIsUpdated() {
        Page page = DriverManager.getPage();
        boolean applied;
        try {
            // A selected swatch shows a checkmark (an <svg> child); 
            page.locator("[id='" + chosenBackgroundId + "']:has(svg)")
                    .waitFor(new Locator.WaitForOptions().setTimeout(10000));
            applied = true;
        } catch (RuntimeException e) {
            applied = false;
        }
        Assertions.assertThat(applied)
                .as("Chosen background swatch should become selected (show the checkmark)").isTrue();
        Assertions.assertThat(chosenBackgroundId)
                .as("A different background than the current one should be selected")
                .isNotEqualTo(previousBackgroundId);
    }

    /** The selected swatch is the radio that contains the checkmark <svg>. */
    private String selectedBackgroundId(Page page) {
        Locator selected = page.locator(RADIO + ":has(svg)");
        return selected.count() > 0 ? selected.first().getAttribute("id") : null;
    }

    private String selectDifferentBackground(Page page, String currentId) {
        Locator options = page.locator(RADIO);
        int count = options.count();
        for (int i = 0; i < count; i++) {
            Locator option = options.nth(i);
            String id = option.getAttribute("id");
            if (currentId == null || !currentId.equals(id)) {
                option.scrollIntoViewIfNeeded();
                option.click();
                return id;
            }
        }
        throw new FrameworkException("No alternative background swatch was available to select.");
    }
}
