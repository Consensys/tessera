package com.quorum.tessera.test.cli;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "com.quorum.tessera.test.cli.version",
    features = "build/resources/test/features/cli/version.feature",
    monochrome = true,
    plugin = {"progress"})
public class CucumberVersionCliIT {}
