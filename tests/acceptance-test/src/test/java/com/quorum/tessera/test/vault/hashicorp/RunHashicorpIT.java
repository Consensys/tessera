package com.quorum.tessera.test.vault.hashicorp;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/vault/hashicorp.feature",
    plugin = {"pretty"})
public class RunHashicorpIT {}
