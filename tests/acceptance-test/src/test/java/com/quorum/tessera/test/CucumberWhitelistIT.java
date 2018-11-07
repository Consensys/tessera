package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/whitelist.feature",
    glue = "transaction.rest",
    tags = "@rest",
    plugin = {"json:target/cucumber/rest.json"}
)
public class CucumberWhitelistIT {

}
