package com.quorum.tessera.test;

import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
    glue = "transaction.rest",
    tags = "@rest",
    plugin = {"json:build/cucumber/rest.json"})
public class CucumberRestIT extends CucumberTestCase {}
