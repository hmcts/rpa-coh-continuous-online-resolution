Feature: Conversations

  Scenario: Submit a whole conversation
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And the notification scheduler runs
    Given a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the notification scheduler runs
    Given a standard decision
    And a POST request is sent for a decision
    When a GET request is sent for a conversation
    Then the response code is 200
    And the conversation response contains an online hearing
    And the conversation response contains an online hearing with the correct uri
    And the conversation response contains an online hearing with state desc of 'Continuous Online Hearing Questions Issued'
    And the conversation response contains an online hearing with 2 history entries
    And the conversation response contains an online hearing with 1 history entry  with state desc of 'Continuous Online Hearing Started'
    And the conversation response contains a decision
    And the conversation response contains a decision with the correct uri
    And the conversation response contains a decision with state desc of 'Decision Drafted'
    And the conversation response contains a decision with 1 history entries
    And the conversation response contains a decision with 1 history entry with state desc of 'Decision Drafted'
    And the conversation response contains 1 question
    And the conversation response contains a question with the correct uri
    And the conversation response contains a question with state desc of 'Question Issued'
    And the conversation response contains a question with 3 history entries
    And the conversation response contains a question with 1 history entry with state desc of 'Question Drafted'
    And the conversation response contains 1 answer
    And the conversation response contains an answer with state desc of 'Answer Drafted'
    And the conversation response contains an answer with the correct uri
    And the conversation response contains an answer with 1 history entries
    And the conversation response contains an answer with 1 history entry with state desc of 'Answer Drafted'

  Scenario: Submit a conversation without decision
    Given a standard online hearing is created
    And a valid question
    And the put request is sent to issue the question round ' "1" '
    And the notification scheduler runs
    Given a standard answer
    And the endpoint is for submitting an answer
    And a POST request is sent
    And the notification scheduler runs
    When a GET request is sent for a conversation
    Then the response code is 200
    And the conversation response contains an online hearing
    And the conversation response contains an online hearing with 2 history entries
    And the conversation response contains an online hearing with the correct uri
    And the conversation response contains no decision
    And the conversation response contains 1 question
    And the conversation response contains a question with the correct uri
    And the conversation response contains a question with 3 history entries
    And the conversation response contains 1 answer
    And the conversation response contains an answer with the correct uri
    And the conversation response contains an answer with 1 history entries

  Scenario: Submit a conversation without question
    Given a standard online hearing is created
    When a GET request is sent for a conversation
    Then the response code is 200
    And the conversation response contains an online hearing
    And the conversation response contains an online hearing with 1 history entries
    And the conversation response contains an online hearing with the correct uri
    And the conversation response contains 0 question
