package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/whitelist.feature",
        glue = "transaction.whitelist",
        tags = "@rest",
        plugin = {"progress"})
public class CucumberWhitelistIT {}
