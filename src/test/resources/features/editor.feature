@editingsteps
Feature: Video editor
  As a logged-in user
  I want to open a video in the editor and use its features
  So that I can review and edit my content

  Background:
    Given the user is logged in

  @smoke @editor
  Scenario: Editor loads with its key elements
    When the user opens an existing video in the editor
    Then the editor page loads with its key elements

  @editor @interaction
  Scenario: Changing the video background applies the new background
    When the user opens an existing video in the editor
    Then the editor page loads with its key elements
    When the user changes the video background
    Then the selected background is updated
