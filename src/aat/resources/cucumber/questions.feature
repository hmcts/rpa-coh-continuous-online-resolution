Feature: Questions feature

  Scenario: Create question
    Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestCreate" '
    When a post request is sent to ' "/"'
    Then the client receives a 200 status code
    And the response contains the following text '"onlineHearingId" '
    And the response contains the following text '"CucumberExternalRefTestCreate" '