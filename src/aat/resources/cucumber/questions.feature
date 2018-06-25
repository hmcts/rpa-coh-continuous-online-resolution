Feature: Questions feature

  Scenario: Create question and assign state to issued
    Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberQuestionOnlineHearing" '
    And the ' "jurisdictionName"' field set to ' "SSCS" '
    When a post request is sent to ' "/online-hearings/"'
    Given the draft a question for online_hearing ' "CucumberQuestionOnlineHearing" '
    Then the question state is ' "DRAFTED" '
    When a patch request is sent to ' "/online-hearings/onlineHearing_id/questions/question_id" ' and response status is ' "Successful" '
    Then the question state is ' "ISSUED" '

  Scenario: Create question and attempt to issue but SSCS endpoint is down
    Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberQuestionOnlineHearing" '
    And the ' "jurisdictionName"' field set to ' "SSCS" '
    When a post request is sent to ' "/online-hearings/"'
    Given the draft a question for online_hearing ' "CucumberQuestionOnlineHearing" '
    Then the question state is ' "DRAFTED" '
    And the SSCS endpoint is invalid
    When a patch request is sent to ' "/online-hearings/onlineHearing_id/questions/question_id" ' and response status is ' "Server error" '