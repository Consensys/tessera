Feature: Config migration utility
    Standalone java app included with Tessera to convert legacy .toml config files to .json

    Scenario: toml to json specifying output file
        Given /legacy.toml exists
        When the Config Migration Utility is run with tomlfile /legacy.toml and --outputfile option
        Then the outputfile is created
        And /legacy.toml and the outputfile are equivalent
