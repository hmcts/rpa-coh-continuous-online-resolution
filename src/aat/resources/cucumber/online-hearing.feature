Feature: Online hearing

  Background:
    Given a standard online hearing is created
    Then the response code is 201

  Scenario: Create online hearing
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID

  Scenario: Create duplicate online hearing
    And a standard online hearing is created
    Then the response code is 409
    Then the response contains the following text '"Duplicate case found" '

  Scenario: Retrieve online hearing
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the response code is 200
    And the response contains the following text '"case_123" '
    And the response contains 1 panel member

  Scenario: Update online hearing state to questions issued
    Given a standard online hearing for update
    And the update online hearing state is continuous_online_hearing_questions_issued
    And a PUT request is sent for online hearings
    Then the response code is 200
    And the response contains the following text '"Online hearing updated" '

  Scenario: Update with invalid online hearing state
    Given a standard online hearing for update
    And the update online hearing state is foo
    And a PUT request is sent for online hearings
    Then the response code is 400
    And the response contains the following text '"Invalid state" '
    
  Scenario: Update with starting state
    Given a standard online hearing for update
    And the update online hearing state is continuous_online_hearing_started
    And a PUT request is sent for online hearings
    Then the response code is 409
    And the response contains the following text '"Online hearing state cannot be changed back to started" '
    
  Scenario: Update non-existent online hearing
    And the update online hearing state is continuous_online_hearing_answers_sent
    And a PUT request is sent for online hearings
    Then the response code is 404
    And the response contains the following text '"Online hearing not found" '