Feature: Question Round Logic

  Scenario: Create question round from 1 to 2
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "2" '
    Then the response code is 200

  Scenario: If no jurisdiction question round limit is set then still validate question round
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "0" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "2" '
    Then the response code is 200
    Given the question round is ' "4" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Attempt to create the first question as question round 2
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create a question round from 2 to 1 is invalid
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "2" '
    Then the response code is 200
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create question round from 1 to 3 is invalid
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given a standard question
    And the question round is ' "3" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create question round from 1 to 3 is invalid when the max jurisdiction is 2
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "2" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    Given a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "2" '
    Then the response code is 200
    Given a standard question
    And the question round is ' "3" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Get all question rounds for online hearing and check state is ISSUED and DRAFTED
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    Given a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the number of questions rounds is ' "2" '
    And the number of questions in question round ' "1" ' is ' "2" '
    And the question round ' "1" ' is ' "question_issued" '
    And the question round ' "2" ' is ' "question_drafted" '

  Scenario: Get all question rounds for online hearing and check the previous, current, next & max QRs are correct
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    And a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the previous question round is ' "1" '
    And the current question round is ' "2" '
    And the next question round is ' "2" '
    And the max question round is ' "2" '

  Scenario: Get a question round for an online hearing
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "1" '
    Then the response code is 200
    When the post request is sent to create the question
    And the get request is sent to get question round ' "1" '
    Then the response code is 200
    And the number of questions in question round ' "1" ' is ' "2" '
    And the question round ' "1" ' is ' "question_drafted" '
    And each question in the question round has a history of at least ' "1" ' events

  Scenario: Issue a question round
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    And the get request is sent to get question round ' "1" '
    Then the response code is 200
    And the question round ' "1" ' is ' "question_issued" '
    And each question in the question round has a history of at least ' "2" ' events

  Scenario: Issue a question round and check all questions are updated
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    And the get request is sent to get question round ' "1" '
    Then the response code is 200
    And the question round ' "1" ' is ' "question_issued" '
    And each question in the question round has a history of at least ' "2" ' events

  Scenario: Attempt to issue a previous question round
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    When a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 422

  Scenario: Attempt to issue a question round which does not exist
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "2" '
    Then the response code is 404

  Scenario: Attempt to re-issue the current question round
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 200
    When the put request is sent to issue the question round ' "1" '
    Then the response code is 422