Feature: Re-listing online hearing

  Scenario: Drafting a reason for re-listing
    Given a standard online hearing is created
    And the relist reason is set to 'Not ready yet'
    When drafting the relist
    Then the online hearing state is 'continuous_online_hearing_started'
    And the relist state should be 'drafted'

  Scenario: Submitting re-listing reason
    Given a standard online hearing is created
    And the relist reason is set to 'Has to be re-listed.'
    When issuing the relist
    And the notification scheduler runs
    And the online hearing state is refreshed
    Then the online hearing state is 'continuous_online_hearing_relisted'
    And the relist state should be 'issued'
