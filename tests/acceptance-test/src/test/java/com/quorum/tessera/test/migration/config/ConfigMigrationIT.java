package com.quorum.tessera.test.migration.config;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/migration/config-migration.feature",
    plugin = {"pretty"})
public class ConfigMigrationIT {
}
