Feature: Synchronizing app/device metadata in the native layer

  Background:
    Given I clear all persistent data

  Scenario: Capture foreground state while in the foreground
    When I run "CXXDelayedNotifyScenario"
    And I wait to receive an error
    Then the error payload contains a completed handled native report
    And the event "app.inForeground" is true
    And the event "app.duration" is greater than 0
    And the event "unhandled" is false

  Scenario: Capture foreground state while in the background
    When I run "CXXBackgroundNotifyScenario"
    And I send the app to the background for 5 seconds
    And I wait to receive an error
    Then the error payload contains a completed handled native report
    And the event "app.inForeground" is false
    And the event "app.durationInForeground" equals 0
    And the event "app.duration" is greater than 0
    And the event "context" string is empty
    And the event "unhandled" is false
    # Appium 1.9.1 - 1.20.2 changes the orientation to landscape when foregrounding.
    # Return it to portrait to avoid impacting other scenarios.
    And I set the screen orientation to portrait

  Scenario: Capture foreground state while in a foreground crash
    When I run "CXXTrapScenario" and relaunch the crashed app
    And I configure Bugsnag for "CXXStartSessionScenario"
    And I wait to receive an error
    Then the error payload contains a completed handled native report
    And the event "app.inForeground" is true
    And the event "app.durationInForeground" is not null
    And the event "app.duration" is not null
    And the event "unhandled" is true

  Scenario: Capture foreground state while in a background crash
    When I run "CXXDelayedCrashScenario"
    And I send the app to the background for 10 seconds
    And I relaunch the app after a crash
    And I configure Bugsnag for "CXXDelayedCrashScenario"
    And I wait to receive an error
    Then the error payload contains a completed handled native report
    And the event "app.inForeground" is false
    And the event "app.duration" is greater than 0
    And the event "context" string is empty
    And the event "unhandled" is true
    # Appium 1.9.1 - 1.20.2 changes the orientation to landscape when foregrounding.
    # Return it to portrait to avoid impacting other scenarios.
    And I set the screen orientation to portrait
