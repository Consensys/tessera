package com.quorum.tessera.test;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/whitelist.feature",
    glue = "transaction.whitelist",
    tags = "@rest",
    plugin = {"progress"}
)
public class CucumberWhitelistIT {

}
