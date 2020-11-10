package com.quorum.tessera.test.vault.aws;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/vault/aws.feature",
    plugin = {"pretty"})
public class RunAwsIT {
}
