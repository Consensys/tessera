package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/transaction.feature",
    glue = "send.rest",
    tags = "@rest",
    monochrome = true,
    plugin = {"pretty"}
)

public class CucumberRestIT {

}
