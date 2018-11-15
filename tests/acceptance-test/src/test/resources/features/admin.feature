Feature: Runtime Admin functions

    Scenario: Admin user adds new peer while node is running
    Given any node is running
    When admin user executes add peer
    Then a peer is added to party

    Scenario: Admin user starts node with empty key values
    Given configuration file with empty public and private key values
    When admin user executes start
    Then node returns error message and exits

    Scenario: Admin user starts node with key paths containing empty values
    Given configuration file with with key paths containing empty values
    When admin user executes start
    Then node returns error message and exits

