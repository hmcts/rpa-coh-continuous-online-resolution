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
    And the decision expiry date empty

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
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    When a GET request is sent for a decision
    And the decision state name is decision_issue_pending
    And the decision expiry date is 7 days in the future
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the online hearing state is 'continuous_online_hearing_started'

  Scenario: Update decision state that's already issued
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the response code is 200
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And the response code is 409
    And the response contains the following text '"Only draft decisions can be updated" '

  Scenario: Reply to a decision and accept it
    Given a standard online hearing is created
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And wait until the event is processed
    Given a standard decision reply
    And a POST request is sent for a decision reply
    And the response code is 201
    Then a GET request is sent for a decision reply
    And the response code is 200
    And the decision reply contains all the fields

  Scenario: Reply to a decision and reject it
    Given a standard online hearing is created
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And wait until the event is processed
    Given a standard decision reply
    And the decision reply is ' "decision_rejected" '
    And a POST request is sent for a decision reply
    And the response code is 201
    Then a GET request is sent for a decision reply
    And the response code is 200
    And the decision reply contains all the fields

  Scenario: Reply to a decision with invalid reply and throw bad request
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And wait until the event is processed
    Given a standard decision reply
    And the decision reply is ' "invalid_reply" '
    And a POST request is sent for a decision reply
    And the response code is 400

  Scenario: Reply to a decision which is not issued and throw not found
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision reply
    And the decision reply is ' "decision_rejected" '
    And a POST request is sent for a decision reply
    And the response code is 404

  Scenario: Get all replies to a decision
    Given a standard online hearing is created
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issue_pending
    And a PUT request is sent for a decision
    And wait until the event is processed
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
    Given a standard online hearing is created
    Given a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    When a GET request is sent for all decision replies
    Then the response code is 200
    And the decision replies list contains 0 decision replies