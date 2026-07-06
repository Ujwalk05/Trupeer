package io.trupeer.automation.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.services.EditorService;
import org.assertj.core.api.Assertions;

/** "Modify Script with AI" flow and its negative cases (drives EditorService). */
public class ScriptAiSteps {

    private final EditorService editor;
    private String originalScript;

    public ScriptAiSteps(EditorService editor) {
        this.editor = editor;
    }

    @When("the user opens the {string} tool")
    public void theUserOpensTheTool(String toolName) {
        // Reset any leftover "Keep/Discard changes" state from a previous scenario.
        editor.discardPendingRewrite();
        originalScript = editor.currentScript();
        editor.openModifyWithAiDialog();

        Assertions.assertThat(editor.isDialogOpen())
                .as("The '%s' dialog (Add Custom Instructions) should open", toolName).isTrue();
    }

    @When("the user submits the prompt {string}")
    public void theUserSubmitsThePrompt(String prompt) {
        editor.enterPrompt(prompt);
        DriverManager.getPage().waitForTimeout(2000);   // pause so the typed prompt is visible
        Assertions.assertThat(editor.isRewriteEnabled())
                .as("Rewrite Script should be enabled once a valid prompt is entered").isTrue();
        editor.clickRewrite();
    }

    @When("the user leaves the prompt empty")
    public void theUserLeavesThePromptEmpty() {
        editor.enterPrompt("");
    }

    @When("the user enters a prompt longer than the limit")
    public void theUserEntersAPromptLongerThanTheLimit() {
        editor.enterPrompt("Make this script more concise and clearer. ".repeat(30));
        DriverManager.getPage().waitForTimeout(2000);   // pause so the capped value is visible
    }

    @Then("a modified script is returned and displayed")
    public void aModifiedScriptIsReturnedAndDisplayed() {
        int aiTimeout = ConfigurationManager.get().aiTimeoutMs();

        boolean changed = editor.waitForScriptToChange(originalScript, aiTimeout);
        Assertions.assertThat(changed)
                .as("Script should be rewritten within %dms. If not, capture the rate-limit/backend "
                        + "error (or exhausted AI minutes) as a bug in the Part 1 report", aiTimeout)
                .isTrue();

        String updatedScript = editor.currentScript();
        Assertions.assertThat(updatedScript)
                .as("Rewritten script should differ from the original").isNotEqualTo(originalScript);
        Assertions.assertThat(updatedScript)
                .as("Rewritten script should not be empty").isNotBlank();

        Assertions.assertThat(editor.reviewControlsVisible())
                .as("Editor should present 'Keep changes' review controls for the returned script")
                .isTrue();

        editor.discardPendingRewrite();  
    }

    @Then("the rewrite action is disabled")
    public void theRewriteActionIsDisabled() {
        Assertions.assertThat(editor.isRewriteEnabled())
                .as("An empty prompt must not be submittable - 'Rewrite Script' should be disabled")
                .isFalse();
    }

    @Then("the prompt is capped at {int} characters")
    public void thePromptIsCappedAtCharacters(int limit) {
        Assertions.assertThat(editor.promptLength())
                .as("Prompt field should enforce a max length of %d characters", limit)
                .isLessThanOrEqualTo(limit);
    }
}
