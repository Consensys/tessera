package com.quorum.tessera.test.migration.config;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/migration/config-migration.feature",
        plugin = {"pretty"})
public class ConfigMigrationIT {

    @BeforeClass
    public static void onSetup() {
        //    only needed when running outside of maven build process
        //        System.setProperty("config-migration-app.jar",
        // "/Users/chris/jpmc-tessera/config-migration/target/config-migration-0.9-SNAPSHOT-cli.jar");
    }
}
