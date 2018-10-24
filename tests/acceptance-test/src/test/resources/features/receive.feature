Feature: Quorum peer requests transaction data

	Scenario: Quorum peer requests existing transaction data
	Given Party as been sent transaction data
	When Party receives request for transaction from Quorum peer
	Then Party returns transaction data

	Scenario: Quorum peer requests transaction data that does not exist
	Given Party as been sent transaction data
	When Party receives request for transaction from Quorum peer
	Then Party data not found error
