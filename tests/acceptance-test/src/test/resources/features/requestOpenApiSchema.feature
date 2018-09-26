Feature: Request openapi schema
    Scenario: Request openapi schema call is successful
        Given that the server is running
        When client makes api request
        Then the server responds with status 200 and body No version defined yet! and contentType text/html
