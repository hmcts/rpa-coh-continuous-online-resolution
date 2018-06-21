Feature: Question

  Scenario: Create a question
    Given a valid online hearing
    When a POST request is sent
    Then the response code is 200
