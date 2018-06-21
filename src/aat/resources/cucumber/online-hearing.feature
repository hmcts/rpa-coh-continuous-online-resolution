Feature: Online hearing

  Scenario: Create online hearing
    Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestCreate" '
    When a post request is sent to ' "/"'
    Then the client receives a 200 status code 
    And the response contains the following text '"onlineHearingId" '
    And the response contains the following text '"CucumberExternalRefTestCreate" '

   Scenario: Read online hearing
     Given SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestRetrieve" '
     And a post request is sent to ' "/"'
     And the client receives a 200 status code
     And the response contains the following text '"CucumberExternalRefTestRetrieve" '
     And SSCS prepare a json request with the ' "externalRef"' field set to ' "CucumberExternalRefTestRetrieve" '
     And a get request is sent to ' "/CucumberExternalRefTestRetrieve"'
     Then the client receives a 200 status code
     And the response contains the following text '"onlineHearingId" '
     And the response contains the following text '"CucumberExternalRefTestRetrieve" '
