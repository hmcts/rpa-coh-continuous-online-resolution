Feature: Event Notification Scheduler

  Scenario: Notification Event For All Answers Submitted
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response headers contains a location to the created entity
    And send get request to the location
    And an event has been queued for this online hearing of event type answers_submitted
    When the notification scheduler runs
    Then the event status is event_forwarding_success

  Scenario: Notification Event Failures
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response headers contains a location to the created entity
    And send get request to the location
    And an event has been queued for this online hearing of event type answers_submitted
    When the notification scheduler fails to send after configured retries for 'SSCS' and event type 'answers_submitted'
    Then the event status is event_forwarding_failed
