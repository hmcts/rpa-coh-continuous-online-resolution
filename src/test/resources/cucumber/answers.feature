Feature: Answers

  Scenario: Submit an answer for unknown question
    Given a valid answer
    And the endpoint is '/online-hearings/1/questions/99/answers'
    When a POST request is sent
    Then the response code is 424

  Scenario: Submit an answer for a question
    Given an existing question with id of 1
    And a valid answer
    And the endpoint is '/online-hearings/1/questions/1/answers'
    When a POST request is sent
    Then the response code is 200

  Scenario: Submit an answer with no answer text
    Given an existing question with id of 1
    Given a valid answer
    And the answer text is empty
    And the endpoint is '/online-hearings/1/questions/1/answers'
    When a POST request is sent
    Then the response code is 422

  Scenario: Submit an answer for a question
    Given an existing question with id of 1
    And a valid answer
    And the endpoint is '/online-hearings/1/questions/1/answers'
    When a POST request is sent
    Then the response code is 200

  Scenario: Update an answer
    Given an existing question with id of 1
    And a valid answer
    And the answer text is 'foo'
    And the endpoint is '/online-hearings/1/questions/1/answers'
    And a POST request is sent
    And the response code is 200
    And an update to the answer is required
    And the answer text is 'bar'
    When a PATCH request is sent
    Then the response code is 200
    And the answer text is 'bar'
