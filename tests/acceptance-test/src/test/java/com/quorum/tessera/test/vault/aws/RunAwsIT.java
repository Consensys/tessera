package com.quorum.tessera.test.vault.aws;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/vault/aws.feature",
    plugin = {"pretty"})
public class RunAwsIT {}
