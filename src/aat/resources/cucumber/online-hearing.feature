Feature: Online hearing

  Background:
    Given a standard online hearing is created
    Then the response code is 201

  Scenario: Create online hearing
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID
    And the response headers contains a location to the created entity
    And send get request to the location
    And the response code is 200

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
    And the panel member name is 'Judge Dredd'
    And the panel member role is 'Judge'

  Scenario: Update online hearing state to relisted
    Given a standard update online hearing request
    And the update online hearing state is continuous_online_hearing_relisted
    And a PUT request is sent for online hearings
    Then the response code is 200
    And the response contains the following text '"Online hearing updated" '

  Scenario: Update with invalid online hearing state
    Given a standard update online hearing request
    And the update online hearing state is foo
    And a PUT request is sent for online hearings
    Then the response code is 422
    
  Scenario: Update with starting state
    Given a standard update online hearing request
    And the update online hearing state is continuous_online_hearing_started
    And a PUT request is sent for online hearings
    Then the response code is 409

  Scenario: Update non-existent online hearing
    And the update online hearing state is continuous_online_hearing_answers_sent
    And the request contains a random UUID
    And a PUT request is sent for online hearings
    Then the response code is 404
