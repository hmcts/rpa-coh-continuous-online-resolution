Feature: Question round logic

  Scenario: Create question round from 1 to 2
    Given a standard online hearing is created
    Given a standard question
    When the question round is ' "1" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "2" '
    Then the post request is sent to create the question and the response status is 200

  Scenario: If no jurisdiction question round limit is set then validate question round
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "0" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    And a standard question
    When the question round is ' "3" '
    Then the post request is sent to create the question and the response status is 200

  Scenario: Attempt to create the first question as question round 2
    Given a standard online hearing is created
    Given a standard question
    When the question round is ' "2" '
    Then the post request is sent to create the question and the response status is 422

  Scenario: Create a question round from 2 to 1 is invalid
    Given a standard online hearing is created
    Given a standard question
    When the question round is ' "1" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "2" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "1" '
    Then the post request is sent to create the question and the response status is 422

  Scenario: Create question round from 1 to 3 is invalid
    Given a standard online hearing is created
    Given a standard question
    When the question round is ' "1" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "3" '
    Then the post request is sent to create the question and the response status is 422

  Scenario: Create question round from 5 to 6 is invalid when the max jurisdiction is 5
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "2" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    Given a standard question
    When the question round is ' "1" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "2" '
    Then the post request is sent to create the question and the response status is 200
    When the question round is ' "3" '
    Then the post request is sent to create the question and the response status is 422