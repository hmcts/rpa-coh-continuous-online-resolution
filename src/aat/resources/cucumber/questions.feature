Feature: Questions feature

  Scenario: Submit a question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200

  Scenario: Retrieve a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question id matches


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
