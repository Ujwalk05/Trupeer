package io.trupeer.automation.services;

import com.microsoft.playwright.Page;
import io.trupeer.automation.actions.ClickActionHelper;
import io.trupeer.automation.actions.InputActionHelper;
import io.trupeer.automation.actions.ValidationHelper;
import io.trupeer.automation.core.config.ConfigurationManager;
import io.trupeer.automation.core.driver.DriverManager;
import io.trupeer.automation.core.exceptions.FrameworkException;
import io.trupeer.automation.pages.DashboardPage;
import io.trupeer.automation.pages.LoginPage;

public class AuthService {

    private final ConfigurationManager cfg = ConfigurationManager.get();
    private final InputActionHelper input = new InputActionHelper();
    private final ClickActionHelper click = new ClickActionHelper();
    private final ValidationHelper validate = new ValidationHelper();

    public void openLoginPage() {
        DriverManager.getPage().navigate(cfg.loginUrl());
        validate.waitForPageLoad();
    }

    public boolean isLoginFormVisible() {
        return validate.waitForVisible(LoginPage.EMAIL, 5000);
    }

    public boolean isOnDashboard() {
        return validate.waitForVisible(DashboardPage.WELCOME_BANNER, 15000);
    }

    public void login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new FrameworkException("Credentials are empty - set TRUPEER_EMAIL and TRUPEER_PASSWORD "
                    + "in THIS terminal before running (env vars don't carry across terminal windows).");
        }
        click.click(LoginPage.LOGIN_TAB);
        input.fillVerified(LoginPage.EMAIL, email);
        input.fillVerified(LoginPage.PASSWORD, password);
        click.click(LoginPage.SUBMIT);
    }

    /**
     * Idempotent: if we're already inside the app, does nothing; otherwise opens
     * the app and authenticates with the configured credentials.
     */
    public void ensureLoggedIn() {
        Page page = DriverManager.getPage();
        if (page.url().contains("trupeer.ai") && !page.url().contains("/auth")) {
            return;
        }
        page.navigate(cfg.baseUrl());
        validate.waitForPageLoad();
        if (isLoginFormVisible()) {
            login(cfg.email(), cfg.password());
        }
    }

    public void logout() {
        click.click(DashboardPage.ACCOUNT_MENU);
        click.click(DashboardPage.LOGOUT);
        validate.waitForPageLoad();
    }
}
