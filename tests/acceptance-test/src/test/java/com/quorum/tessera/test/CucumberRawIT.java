package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/transaction.feature",
    glue = "send.raw",
    tags = "@raw",
    strict = true,
    monochrome = true,
    plugin = {"pretty"}
)
public class CucumberRawIT {

}
