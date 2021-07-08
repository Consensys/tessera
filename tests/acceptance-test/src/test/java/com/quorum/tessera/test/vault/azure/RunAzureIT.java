package com.quorum.tessera.test.vault.azure;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "build/resources/test/features/vault/azure.feature",
    plugin = {"pretty"})
public class RunAzureIT {}
