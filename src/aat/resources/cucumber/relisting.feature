Feature: Re-listing online hearing

  Scenario: Drafting a reason for re-listing
    Given a standard online hearing is created
    When drafting the relist with reason 'Not ready yet'
    Then the online hearing state is 'continuous_online_hearing_started'
    And the relist state should be 'drafted'

  Scenario: Submitting re-listing reason
    Given a standard online hearing is created
    When issuing the relist
    And the notification scheduler runs
    And the online hearing state is refreshed
    Then the online hearing state is 'continuous_online_hearing_relisted'
    And the relist state should be 'issued'

  Scenario: Changes are included in history
    Given a standard online hearing is created
    When drafting the relist with reason 'Anything'
    And issuing the relist with reason ''
    And the notification scheduler runs
    And the online hearing state is refreshed
    Then the online hearing relist history has 3 entries
