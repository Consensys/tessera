package com.quorum.tessera.test.vault.hashicorp;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "build/resources/test/features/vault/hashicorp.feature",
    plugin = {"pretty"}
)
public class RunHashicorpIT {
}
