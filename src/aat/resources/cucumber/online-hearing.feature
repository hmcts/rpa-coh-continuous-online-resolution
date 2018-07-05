Feature: Online hearing

  Scenario: Create online hearing
    Given a standard online hearing is created
    Then the response code is 201
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID

  Scenario: Retrieve online hearing
    Given a standard online hearing is created
    Then the response code is 201
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID
    When a get request is sent to ' "/online-hearings"' for the saved online hearing
    Then the response code is 200
    And the response contains the following text '"case_123" '
    And the response contains 1 panel member

  Scenario: Search for online hearing by case idf
    Given a standard online hearing is created
    And the response code is 201
    When a get request is sent to ' "/online-hearings?case_id=case_123"' for the online hearing
    Then the response code is 200

