Feature: Answers

  Scenario: Submit an answer for unknown question
    Given a valid answer
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 424

  Scenario: Submit an answer for a question
    Given a valid question
    And a valid answer
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 200

  Scenario: Submit an answer with no answer text
    Given a valid question
    Given a valid answer
    And the answer text is empty
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 422

  Scenario: Submit an answer for a question
    Given a valid question
    And a valid answer
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 200

  Scenario: Update an answer
    Given a valid question
    And a valid answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 200
    And an update to the answer is required
    And the answer text is 'bar'
    When a PATCH request is sent
    Then the response code is 200
    And the answer text is 'bar'

  Scenario: Update unknown answer
    Given a valid question
    And an unknown answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a PATCH request is sent
    And the response code is 404
