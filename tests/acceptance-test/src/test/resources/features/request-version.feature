Feature: Upcheck
    Scenario: Upcheck call is successful
        Given that the server is running
        When client makes version request
        Then the server responds with status 200 and body No version defined yet! and contentType text/plain
