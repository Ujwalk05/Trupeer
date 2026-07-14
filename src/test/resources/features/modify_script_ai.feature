@Modify
Feature: Modify Script with AI
  As a logged-in user editing a video
  I want to modify the script using AI
  So that I can quickly refine my narration

Scenario: Successful login lands on the dashboard
    Given the user is logged in
    When the user opens an existing video in the editor
    Then the editor page loads with its key elements

  @smoke @ai @ai-generate
  Scenario: AI returns a modified script for a valid prompt
    When the user opens the "Modify Script with AI" tool
    And the user submits the prompt "Make this script more concise"
    Then a modified script is returned and displayed

  @negative @ai
  Scenario: Prompt is limited to 300 characters
    When the user opens the "Modify Script with AI" tool
    And the user enters a prompt longer than the limit
    Then the prompt is capped at 300 characters
