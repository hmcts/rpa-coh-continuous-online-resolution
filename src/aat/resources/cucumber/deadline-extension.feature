Feature: Deadline Extension

  Scenario: Automatically accepting for the first extension request
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And a standard question
    And the post request is sent to create the question
    When the put request is sent to issue the question round ' "1" '
    And deadline extension is requested
    Then the response code is 200
    And questions' deadlines have been question_deadline_extension_granted

  Scenario: Cannot extend deadline without questions
    Given a standard online hearing is created
    And deadline extension is requested
    Then the response code is 424

  Scenario: Extending already extended deadline is denied
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And a standard question
    And the post request is sent to create the question
    When the put request is sent to issue the question round ' "1" '
    And deadline extension is requested
    And deadline extension is requested again
    Then the response code is 200
    And questions' deadlines have been question_deadline_extension_denied
