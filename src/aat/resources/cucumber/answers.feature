Feature: Answers

#  Scenario: Submit an answer for unknown question
#    Given a standard online hearing is created
#    Given a standard answer
#    And the endpoint is for submitting an answer
#    When a POST request is sent
#    Then the response code is 424
#
#  Scenario: Submit an answer for a question
#    Given a standard online hearing is created
#    Given a valid question
#    And a standard answer
#    And the endpoint is for submitting an answer
#    When a POST request is sent
#    Then the response code is 200
#
#  Scenario: Submit an answer with no answer text
#    Given a standard online hearing is created
#    Given a valid question
#    Given a standard answer
#    And the answer text is empty
#    And the endpoint is for submitting an answer
#    When a POST request is sent
#    Then the response code is 422
#
#  Scenario: Submit an answer for a question
#    Given a standard online hearing is created
#    Given a valid question
#    And a standard answer
#    And the endpoint is for submitting an answer
#    When a POST request is sent
#    Then the response code is 200
#
#  Scenario: Update an answer
#    Given a standard online hearing is created
#    Given a valid question
#    And a standard answer
#    And the answer text is 'foo'
#    And the endpoint is for submitting an answer
#    And a POST request is sent
#    And the response code is 200
#    And an update to the answer is required
#    And the answer text is 'bar'
#    When a PATCH request is sent
#    Then the response code is 200
#    And the answer text is 'bar'
#
#  Scenario: Update unknown answer
#    Given a standard online hearing is created
#    Given a valid question
#    And a standard answer
#    And an unknown answer identifier
#    And the answer text is 'foo'
#    And the endpoint is for submitting an answer
#    And a PATCH request is sent
#    And the response code is 404

  Scenario: Return multiple answers
    Given a standard online hearing is created
    Given a valid question
    And a standard answer
    And the answer text is 'foo'
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the answer text is 'bar'
    And a POST request is sent
    And the response code is 200
    And the endpoint is for submitting all answer
    When a GET request is sent
    Then there are 2 answers