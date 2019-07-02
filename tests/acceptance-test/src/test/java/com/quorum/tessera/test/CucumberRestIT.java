package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(
        glue = "transaction.rest",
        tags = "@rest",
        plugin = {"json:target/cucumber/rest.json"})
public class CucumberRestIT extends CucumberTestCase {}
