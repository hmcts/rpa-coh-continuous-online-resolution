Feature: Online hearing
  Scenario: Create online hearing
    Given 'SSCS' prepare a json request
    And set the 'external_id' to 'test_externalId12'
    When a post request is sent to '/online-hearings/create'
    Then the response contains the following text '"onlineHearingId": '
    And the response contains the following text '"externalRef": '
