Feature: Event features

  Scenario: Update decision state to issued
    Given a standard online hearing is created
    Then the response code is 201
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the online hearing state is 'continuous_online_hearing_started'
    And  an event has been queued for this online hearing of event type decision_issued

  Scenario: Subscribe to a duplicate event
    Given a conflicting request to subscribe to question round issued
    When a POST request is sent to register
    Then the response code is 409

  @events
  Scenario: Subscribe to an event
    Given a standard event register request
    And jurisdiction ' "PIP", with id ' "42" ' and max question rounds ' "5" ' is created
    When a POST request is sent to register
    Then the response code is 200

  Scenario: Subscribe to an event with invalid jurisdiction
    Given a standard event register request
    And an invalid '"jurisdiction"'
    When a POST request is sent to register
    Then the response code is 422

  Scenario: Subscribe to an event with invalid event type
    Given a standard event register request
    And an invalid '"eventType"'
    When a POST request is sent to register
    Then the response code is 422

  Scenario: Subscribe to an event with invalid url
    Given a standard event register request
    And an invalid '"url"'
    When a POST request is sent to register
    Then the response code is 422



