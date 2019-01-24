Feature: Distinct Notifications

  Scenario: Creating two same events
    Given a standard online hearing is created
    When 2 question_round_issued events are added
    Then there are 1 question_round_issued events in the queue
