Feature: Question round

  Scenario: Set question round state to issued and alert the jurisdiction
    When a get request is sent to ' "/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questionrounds/f9648584-4aa5-4cb0-aba6-d2633ad5a375"'
    Then the client receives a 200 status code
    And the response contains the following text '"f9648584-4aa5-4cb0-aba6-d2633ad5a375" '
    And the response contains the following text '"questionRoundId" '
