Feature: Decisions features

  Background:
    Given a standard online hearing is created
    Then the response code is 201

  Scenario: Create decision
    And a standard decision
    When a POST request is sent for a decision
    Then the response code is 201
    And the response contains the decision UUID
    And the response headers contains a location to the created entity
    And send get request to the location
    And the response code is 200

  Scenario: Retrieve a decision
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    When a GET request is sent for a decision
    Then the response code is 200
    And the decision id matches
    And the decision state name is decision_drafted
    And the decision state timestamp is today

  Scenario: Update with invalid decision state
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is foo
    When a PUT request is sent for a decision
    Then the response code is 422

  Scenario: Update decision state to issued
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issued
    And a PUT request is sent for a decision
    When a GET request is sent for a decision
    And the decision state name is decision_issued
    And the decision expiry date is 7 days in the future

  Scenario: Update decision state that's already issued
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issued
    And a PUT request is sent for a decision
    And the response code is 200
    Given a standard decision for update
    And the update decision state is decision_issued
    And a PUT request is sent for a decision
    And the response code is 409
    And the response contains the following text '"Only draft decisions can be updated" '
