package com.quorum.tessera.test.migration.config;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/migration/config-migration.feature",
        plugin = {"pretty"})
public class ConfigMigrationIT {}
