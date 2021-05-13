package com.quorum.tessera.test.cli;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "com.quorum.tessera.test.cli.keygen",
    features = "build/resources/test/features/cli/file_keygen.feature",
    monochrome = true,
    plugin = {"progress"})
public class CucumberFileKeyGenerationIT {}
