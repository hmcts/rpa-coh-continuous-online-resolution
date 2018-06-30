Feature: Online hearing

  Scenario: Create online hearing
    Given a standard online hearing is created
    Then the client receives a 201 status code
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID

  Scenario: Retrieve online hearing
    Given a standard online hearing is created
    Then the client receives a 201 status code
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID
    When a get request is sent to ' "/online-hearings"' for the saved online hearing
    Then the client receives a 200 status code
    And the response contains the following text '"case_123" '
    And the response contains 1 panel member
