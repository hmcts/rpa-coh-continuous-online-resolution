Feature: Event features

  Scenario: Update decision state to issued
    Given a standard online hearing is created
    Then the response code is 201
    And a standard decision
    And a POST request is sent for a decision
    And the response code is 201
    Given a standard decision for update
    And the update decision state is decision_issued
    And a PUT request is sent for a decision
    When a GET request is sent for a decision
    And the decision state name is decision_issued
    And the decision expiry date is 7 days in the future
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the online hearing state is 'continuous_online_hearing_decision_issued'
    And  an event has been queued for this online hearing of event type decision_issued
