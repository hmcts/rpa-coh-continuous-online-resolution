Feature: Online hearing
  Scenario: Create online hearing
    Given SSCS prepare a json request
    And set the 'externalRef' field to ' "CucumberExternalRefTest" '
    When a post request is sent to /online-hearings/create
    Then the client receives a 200 status code 
    And the response contains the following text '"onlineHearingId" '
    And the response contains the following text '"externalRef" '
