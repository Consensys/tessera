package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(
        glue = "transaction.raw",
        tags = "@raw",
        plugin = {"json:target/cucumber/raw.json"})
public class CucumberRawIT extends CucumberTestCase {}
