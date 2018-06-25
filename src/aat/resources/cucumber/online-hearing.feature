Feature: Online hearing

  Scenario: Create online hearing
    Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestCreate" '
    And the ' "jurisdictionName"' field set to ' "SSCS" '
    When a post request is sent to ' "/online-hearings/"'
    Then the client receives a 200 status code
    And the response contains the following text '"onlineHearingId" '
    And the response contains the following text '"CucumberExternalRefTestCreate" '

  Scenario: Read online hearing
   Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestRetrieve1" '
    And the ' "jurisdictionName"' field set to ' "SSCS" '
   And a post request is sent to ' "/online-hearings/"'
   And the client receives a 200 status code
   And the response contains the following text '"CucumberExternalRefTestRetrieve1" '
   And SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestRetrieve1" '
   And a get request is sent to ' "/online-hearings/CucumberExternalRefTestRetrieve1"'
   Then the client receives a 200 status code
   And the response contains the following text '"onlineHearingId" '
   And the response contains the following text '"CucumberExternalRefTestRetrieve1" '
