Feature: Questions feature

  Scenario: Submit a question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200

  Scenario: No questions to retrieve for online hearing
    Given a standard online hearing is created
    When the get request is sent to retrieve all questions
    Then the response code is 200
    And the response contains 0 questions

  Scenario: Submit multiple questions
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And a standard question
    And the post request is sent to create the question
    When the get request is sent to retrieve all questions
    Then the response code is 200
    And the response contains 2 questions

  Scenario: Edit the question body
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
    Given the question body is edited to ' "some new question text?" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to get the question
    Then the response code is 200
    And the question body is ' "some new question text?" '

  Scenario: Get a single question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
    When the get request is sent to get the question
    Then the response code is 200