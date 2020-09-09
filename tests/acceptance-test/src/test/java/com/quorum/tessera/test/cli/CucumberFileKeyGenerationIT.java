package com.quorum.tessera.test.cli;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "com.quorum.tessera.test.cli.keygen",
    features = "classpath:features/cli/file_keygen.feature",
    monochrome = true,
    plugin = {"progress"})
public class CucumberFileKeyGenerationIT {
}
