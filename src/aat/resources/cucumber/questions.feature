Feature: Questions feature

  Scenario: Submit a question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
#
#  Scenario: Create question and assign state to issued
#    Given a standard online hearing is created
#    Given the draft a question for online_hearing
#    Then the question state is ' "DRAFTED" '
#    When a patch request is sent to ' "/online-hearings/onlineHearing_id/questions/question_id" ' and response status is ' "Successful" '
#    Then the question state is ' "ISSUED" '
#
#  Scenario: Create question and attempt to issue but SSCS endpoint is down
#    Given a standard online hearing is created
#    Given the draft a question for online_hearing
#    And the ' "jurisdictionName"' field set to ' "SSCSDown" '
#    When a post request is sent to ' "/online-hearings/"'
#    Then the question state is ' "DRAFTED" '
#    When a patch request is sent to ' "/online-hearings/onlineHearing_id/questions/question_id" ' and response status is ' "Server error" '