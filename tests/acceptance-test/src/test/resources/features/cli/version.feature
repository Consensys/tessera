Feature: Version CLI

    Scenario: User can get distribution version from CLI
        When Tessera is started with "version" subcommand
        Then the distribution version is printed to stdout
        And the distribution version is in CalVer format
