package com.quorum.tessera.test.vault.azure;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/vault/azure.feature",
    plugin = {"pretty"})
public class RunAzureIT {}
