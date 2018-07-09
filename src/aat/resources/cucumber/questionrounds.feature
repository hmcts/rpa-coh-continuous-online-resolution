Feature: Question Round Logic

  Scenario: Create question round from 1 to 2
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    And the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    And the response code is 200

  Scenario: If no jurisdiction question round limit is set then validate question round
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "0" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
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
    Given the question round is ' "2" '
    When the post request is sent to create the question
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
    Given the question round is ' "3" '
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
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "3" '
    When the post request is sent to create the question
    Then the response code is 422