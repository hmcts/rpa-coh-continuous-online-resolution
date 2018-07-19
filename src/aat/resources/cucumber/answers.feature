Feature: Answers

  Scenario: Submit an answer for unknown question
    Given a standard online hearing is created
    Given a standard answer
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 404

  Scenario: Submit an answer for a question
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 201
    And the response headers contains a location to the created entity
    And send get request to the location
    And the response code is 200

  Scenario: Retrieve an answer for a question
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 201
    And the endpoint is for retrieving an answer
    When a GET request is sent
    And the response code is 200
    And the answer response answer text is 'string'

  Scenario: Submit an answer with no answer text
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the answer text is empty
    And the endpoint is for submitting an answer
    When a POST request is sent
    Then the response code is 422

  Scenario: Update an answer
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 201
    And an update to the answer is required
    And the answer text is 'bar'
    When a PUT request is sent
    Then the response code is 200
    And the answer text is 'bar'

  Scenario: Update an answer state
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 201
    And an update to the answer is required
    And the answer state is 'answer_submitted'
    When a PUT request is sent
    Then the response code is 200

  Scenario: Try and update an answer state from submitted to draft
    Given a standard online hearing is created
    Given a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the response code is 201
    And an update to the answer is required
    And the answer state is 'answer_submitted'
    When a PUT request is sent
    Then the response code is 200
    And the answer state is 'answer_drafted'
    When a PUT request is sent
    Then the response code is 422

  Scenario: Update unknown answer
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And an unknown answer identifier
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    When a PUT request is sent
    Then the response code is 404

  Scenario: Attempt to create multiple answers
    Given a standard online hearing is created
    Given a valid question
    And the put request is sent to issue the question round ' "1" '
    And a standard answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the answer text is 'bar'
    When a POST request is sent
    And the response code is 409
