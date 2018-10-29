Feature: Runtime Admin functions

    Scenario: Admin user adds new peer while node is running
    Given any node is running
    When admin user executes add peer
    Then a peer is added to party
