Feature: CLI file key generation

    Scenario: User generates unlocked keys with all default settings
        Given the application is available
        Given no password
        Given no file path is provided
        When new keys are generated
        Then the application has an exit code of 0
        And public key file ".pub" exists and private key file ".key" exists
        And the generated keys are valid

    Scenario: User attempts to generate unlocked keys when files already exist, but fails
        Given the application is available
        Given no password
        Given no file path is provided
        When new keys are generated
        Then the application has an exit code of 0
        And public key file ".pub" exists and private key file ".key" exists
        When new keys are generated
        Then the application has an exit code of 2

    Scenario: User provides a single relative path for the files
        Given the application is available
        Given no password
        Given a file path of "./newkeys"
        When new keys are generated
        Then the application has an exit code of 0
        And public key file "./newkeys.pub" exists and private key file "./newkeys.key" exists
        And the generated keys are valid

    Scenario: User provides a single absolute path for the files
        Given the application is available
        Given no password
        Given no file exists at "/tmp/newkeys.pub"
        Given no file exists at "/tmp/newkeys.key"
        Given a file path of "/tmp/newkeys"
        When new keys are generated
        Then the application has an exit code of 0
        And public key file "/tmp/newkeys.pub" exists and private key file "/tmp/newkeys.key" exists
        And the generated keys are valid

    Scenario: User provides multiple relative paths for keys
        Given the application is available
        Given no password
        Given a file path of "./newkeys,./otherkeys"
        When new keys are generated
        Then the application has an exit code of 0
        And public key file "./newkeys.pub" exists and private key file "./newkeys.key" exists
        And public key file "./otherkeys.pub" exists and private key file "./otherkeys.key" exists
        And the generated keys are valid

    Scenario: User generates locked keys with all default settings
        Given the application is available
        Given a password of "test"
        Given no file path is provided
        When new keys are generated
        Then the application has an exit code of 0
        And public key file ".pub" exists and private key file ".key" exists
        And the generated keys are valid
