Feature: Reply to decisions
  Background:
    Given a standard online hearing is created
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201

  Scenario: Reply to a decision and accept it
    Given clear decision replies
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the notification scheduler runs
    Given a standard decision reply
    And a POST request is sent for a decision reply
    And the response code is 201
    Then a GET request is sent for a decision reply
    And the response code is 200
    And the decision reply contains all the fields

  Scenario: Reply to a decision and reject it
    Given clear decision replies
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the notification scheduler runs
    Given a standard decision reply
    And the decision reply is ' "decision_rejected" '
    And a POST request is sent for a decision reply
    And the response code is 201
    Then a GET request is sent for a decision reply
    And the response code is 200
    And the decision reply contains all the fields

  Scenario: Reply to a decision with invalid reply and throw bad request
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the notification scheduler runs
    Given a standard decision reply
    And the decision reply is ' "invalid_reply" '
    And a POST request is sent for a decision reply
    And the response code is 400

  Scenario: Reply to a decision which is not issued and throw not found
    Given a standard decision reply
    And the decision reply is ' "decision_rejected" '
    And a POST request is sent for a decision reply
    And the response code is 404

  Scenario: Get all replies to a decision
    Given clear decision replies
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the notification scheduler runs
    Given a standard decision reply
    And the decision reply is ' "decision_rejected" '
    And a POST request is sent for a decision reply
    And the response code is 201
    Given a standard decision reply
    And a POST request is sent for a decision reply
    And the response code is 201
    When a GET request is sent for all decision replies
    Then the response code is 200
    And the decision replies list contains 2 decision replies

  Scenario: Get all replies to a decision with no replies
    Given clear decision replies
    When a GET request is sent for all decision replies
    Then the response code is 200
    And the decision replies list contains 0 decision replies
