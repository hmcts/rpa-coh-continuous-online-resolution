Feature: Answers

  Scenario: Submit an answer for a question
    Given a valid answer
    And the endpoint is '/online-hearings/1/questions/1/answers'
    When a post request is sent
    Then the response code is 200
