@LoggingIn
Feature: Login
  As a Trupeer user
  I want to log into my account
  So that I can access my dashboard and videos

  @smoke @login
  Scenario: Successful login lands on the dashboard, then logout
    Given the user is on the Trupeer login page
    When the user logs in with valid credentials
    Then the user lands on the dashboard
    When the user logs out
    Then the user is returned to the login page

  @negative @login
  Scenario: Login is rejected with invalid credentials
    Given the user is on the Trupeer login page
    When the user logs in with an invalid password
    Then login is rejected with an error
