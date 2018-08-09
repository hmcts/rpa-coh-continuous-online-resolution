Feature: Event Notification Scheduler

  Scenario: Notification Event For Question Round Issued
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And an event has been queued for this online hearing of event type question_round_issued
    When the notification scheduler runs
    Then the event status is event_forwarding_success

  Scenario: Notification Event Failures
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And an event has been queued for this online hearing of event type question_round_issued
    When the notification scheduler fails to send after configured retries for 'SSCS' and event type 'question_round_issued'
    Then the event status is event_forwarding_failed

  Scenario: Notification for Question Deadline Extension Granted
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And a standard question
    And the post request is sent to create the question
    When the put request is sent to issue the question round ' "1" '
    And deadline extension is requested
    And question states are question_deadline_extension_granted
    And an event has been queued for this online hearing of event type question_deadline_extension_granted
    When the notification scheduler runs
    Then the event status is event_forwarding_success
