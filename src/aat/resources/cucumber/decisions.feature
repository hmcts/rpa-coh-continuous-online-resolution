Feature: Decisions features

  Background:
    Given a standard online hearing is created
    Then the response code is 201

  Scenario: Create decision
    And a standard decision
    When a POST request is sent for a decision
    Then the response code is 201
    And the response contains the decision UUID

  Scenario: Retrieve a decision
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    When a GET request is sent for a decision
    Then the response code is 200
    And the decision id matches
    And the decision state name is decision_drafted
    And the decision state timestamp is today

