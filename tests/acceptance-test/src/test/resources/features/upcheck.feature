Feature: Upcheck
    Scenario: Upcheck call is successful
        Given that the server is running
        When client makes upcheck request
        Then the server responds with status 200 and body I'm up! and contentType text/plain
