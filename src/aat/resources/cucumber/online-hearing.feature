Feature: Online hearing

  Background:
    Given a standard online hearing is created
    And the response code is 201
    And the response contains the following text '"online_hearing_id" '

  Scenario: Create online hearing
    And the response contains the online hearing UUID

  Scenario: Retrieve online hearing
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the response code is 200
    And the response contains the following text '"case_123" '
    And the response contains 1 panel member

