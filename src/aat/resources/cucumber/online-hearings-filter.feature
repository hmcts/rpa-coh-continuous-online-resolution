Feature: Online hearing filter feature

  Scenario: Search for non-existent online hearing
    Given a standard online hearing is created
    When a get request is sent to ' "/continuous-online-hearings?case_id=foo"' for the online hearing
    Then the response code is 200
    And the response contains 0 online hearings

  Scenario: Search for an online hearing by case id
    Given a standard online hearing is created
    When a get request is sent to ' "/continuous-online-hearings?case_id=case_123"' for the online hearing
    Then the response code is 200
    And the response contains 1 online hearings
    And the response contains online hearing with case 'case_123'
