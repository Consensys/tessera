Feature: Whitelisting

    @rest
    Scenario: Node receives request from a host not in the whitelist
    Given Node at port 7070
	When a request is made against the node
	Then the response code is UNAUTHORIZED
    Then the node is stopped
