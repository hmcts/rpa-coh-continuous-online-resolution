Feature: Questions feature

  Scenario: Submit a question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 201
    And the header contains location of created question
    And send get request to the location
    And the response code is 200

  Scenario: Retrieve a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question id matches
    And the question state name is question_drafted
    And the question state timestamp is today

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
    Then the response code is 201
    Given the question body is edited to ' "some new question text?" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question body is ' "some new question text?" '

  Scenario: Edit the question header
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 201
    Given the question header is edited to ' "some new header text?" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question header is ' "some new header text?" '

  Scenario: Attempt to edit a question which has been issued
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 201
    When the put request is sent to issue the question round ' "1" '
    Given the question header is edited to ' "some new header text?" '
    When the put request to update the question is sent
    Then the response code is 422

  Scenario: Attempt to Edit the question state to issued
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 201
    Given the question state is edited to ' "ISSUED" '
    When the put request to update the question is sent
    Then the response code is 422

  Scenario: Delete a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And the get request is sent to retrieve the submitted question
    And the response code is 200
    When the delete question request is sent
    Then the response code is 200

  Scenario: Delete a deleted a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And the get request is sent to retrieve the submitted question
    And the response code is 200
    When the delete question request is sent
    Then the response code is 200
    When the delete question request is sent
    Then the response code is 204

  Scenario: Retrieve a deleted a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And the get request is sent to retrieve the submitted question
    And the response code is 200
    When the delete question request is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 404
