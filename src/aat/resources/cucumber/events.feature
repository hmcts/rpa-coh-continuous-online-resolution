Feature: Event features

#  Scenario: Update decision state to issued
#    Given a standard online hearing is created
#    Then the response code is 201
#    And a standard decision
#    And a POST request is sent for a decision
#    And the response code is 201
#    Given a standard decision for update
#    And the update decision state is decision_issue_pending
#    And a PUT request is sent for a decision
#    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
#    Then the online hearing state is 'continuous_online_hearing_started'
#    And  an event has been queued for this online hearing of event type decision_issued
#
#  Scenario: Subscribe to an event
#    Given a standard request to subscribe to question round issued
#    When a POST request is sent to register
#    Then the response code is 200

  Scenario: Reset answer_submitted event state
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And wait until the event is processed
    And a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 201
    And an event has been queued for this online hearing of event type answers_submitted
    And wait until the event is processed
    When the put request is sent to reset the events of type answers_submitted
    Then the response code is 200
    And the event has been set to forwarding_state_pending of event type answers_submitted
