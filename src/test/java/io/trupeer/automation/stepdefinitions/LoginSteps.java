package io.trupeer.automation.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.trupeer.automation.actions.ValidationHelper;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.pages.LoginPage;
import io.trupeer.automation.services.AuthService;
import org.assertj.core.api.Assertions;

/** Login, logout and the shared "ensure logged in" background step (drives AuthService). */
public class LoginSteps {

    private final ConfigurationManager cfg = ConfigurationManager.get();
    private final AuthService auth;
    private final ValidationHelper validate;

    public LoginSteps(AuthService auth, ValidationHelper validate) {
        this.auth = auth;
        this.validate = validate;
    }

    @Given("the user is on the Trupeer login page")
    public void theUserIsOnTheLoginPage() {
        auth.openLoginPage();
    }

    @Given("the user is logged in")
    public void theUserIsLoggedIn() {
        auth.ensureLoggedIn();
        Assertions.assertThat(auth.isOnDashboard())
                .as("Expected to be logged in, but the dashboard did not load. URL: %s",
                        DriverManager.getPage().url())
                .isTrue();
    }

    @When("the user logs in with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        
        if (auth.isLoginFormVisible()) {
            auth.login(cfg.email(), cfg.password());
        }
    }

    @When("the user logs in with an invalid password")
    public void theUserLogsInWithAnInvalidPassword() {
        auth.login(cfg.email(), "definitely-wrong-password-" + System.currentTimeMillis());
    }

    @When("the user logs out")
    public void theUserLogsOut() {
        auth.logout();
    }

    @Then("the user lands on the dashboard")
    public void theUserLandsOnTheDashboard() {
        Assertions.assertThat(auth.isOnDashboard())
                .as("Expected the dashboard to load after login. URL: %s", DriverManager.getPage().url())
                .isTrue();
    }

    @Then("the user is returned to the login page")
    public void theUserIsReturnedToTheLoginPage() {
        boolean backOnLogin = auth.isLoginFormVisible() || DriverManager.getPage().url().contains("auth");
        Assertions.assertThat(backOnLogin)
                .as("Expected to be back on the login page after logout. URL: %s", DriverManager.getPage().url())
                .isTrue();
    }

    @Then("login is rejected with an error")
    public void loginIsRejectedWithAnError() {
        Assertions.assertThat(validate.waitForVisible(LoginPage.ERROR, 10000))
                .as("Expected the 'Wrong email or password.' error for invalid credentials").isTrue();
        Assertions.assertThat(validate.getText(LoginPage.ERROR).toLowerCase())
                .as("Login error message").contains("wrong email or password");
        Assertions.assertThat(DriverManager.getPage().url())
                .as("Invalid credentials must keep the user on the auth page").contains("auth");
    }
}
