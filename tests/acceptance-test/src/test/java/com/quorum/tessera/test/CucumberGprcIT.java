package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(
    glue = "transaction.grpc",
    tags = "@grpc",
    plugin = {"json:target/cucumber/grpc.json"}
)
public class CucumberGprcIT extends CucumberTestCase {

}
