Feature: Store and forward transactions. 

    @raw
    @rest
	Scenario: Party sends transaction to single recipient
	Given Sender party A
    And Recipient party B
    And all parties are running
	When sender party receives transaction from Quorum peer
	Then sender party stores the transaction 
    And forwards the transaction to recipient party

    @raw
    @rest
	Scenario: Party sends transaction to multiple recipients 
	Given Sender party B
    And Recipient parties A,C and D
    And all parties are running
	When sender party receives transaction from Quorum peer
	Then sender party stores the transaction 
	And forwards the transaction to recipient parties
 
    @raw
    @rest
	Scenario: Party sends transaction with no sender defined to a single recipient
    Given Sender party D
    And Recipient party A
    And all parties are running
	When sender party receives transaction with no sender key defined from Quorum peer
	Then sender party stores the transaction 
	And forwards the transaction to recipient parties

    @raw
    @rest
	Scenario: Party sends transaction with no recipients defined
	Given Sender party A
	When sender party receives transaction from Quorum peer
	Then sender party stores the transaction 
	And does not forward transaction to any recipients

    @raw
    @rest
    Scenario: Party sends transaction with no transaction payload
    Given Sender party B
    And Recipient parties A and C
    And all parties are running
	When sender party receives transaction with no payload from Quorum peer
	Then an invalid request error is raised 

    @raw
    @rest
    Scenario: Party sends transaction to unknown recipient
	Given Sender party A
    And all parties are running
	When sender party receives transaction with an unknown party from Quorum peer
	Then an invalid request error is raised

    Scenario: Party/Node goes offline and then synchronises transactions with peers
    Given Sender party A
    And party B is stopped
	When sender party receives transaction from Quorum peer
	Then sender party stores the transaction 
