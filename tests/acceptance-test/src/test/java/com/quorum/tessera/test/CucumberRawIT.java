package com.quorum.tessera.test;

import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
    glue = "transaction.raw",
    tags = "@raw",
    plugin = {"json:build/cucumber/raw.json"})
public class CucumberRawIT extends CucumberTestCase {}
