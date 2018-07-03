Feature: Question round logic

  Scenario: Update question round from 1 to 2
    Given a standard online hearing is created
    Given a standard question
    And the question round is ' "1" '
    And the post request is sent to create the question and the response status is 200
    Given the question round is ' "2" '
    And the post request is sent to create the question and the response status is 200

  Scenario: Attempt to create the first question as question round 1
    Given a standard online hearing is created
    Given a standard question
    And the question round is ' "2" '
    And the post request is sent to create the question and the response status is 422

  Scenario: Update question round from 2 to 1 is invalid
    Given a standard online hearing is created
    Given a standard question
    And the question round is ' "1" '
    And the post request is sent to create the question and the response status is 200
    And the question round is ' "2" '
    And the post request is sent to create the question and the response status is 200
    When the question round is ' "1" '
    And the post request is sent to create the question and the response status is 422

  Scenario: Update question round from 1 to 3 is invalid
    Given a standard online hearing is created
    Given a standard question
    And the question round is ' "1" '
    And the post request is sent to create the question and the response status is 200
    When the question round is ' "3" '
    And the post request is sent to create the question and the response status is 422

  Scenario: Update question round from 5 to 6 is invalid when the max jurisdiction is 5
    Given a standard online hearing is created
    Given a standard question
    And the question round is ' "1" '
    And the post request is sent to create the question and the response status is 200
    And the question round is ' "2" '
    And the post request is sent to create the question and the response status is 200
    And the question round is ' "3" '
    And the post request is sent to create the question and the response status is 200
    And the question round is ' "4" '
    And the post request is sent to create the question and the response status is 200
    And the question round is ' "5" '
    And the post request is sent to create the question and the response status is 200
    When the question round is ' "6" '
    And the post request is sent to create the question and the response status is 422