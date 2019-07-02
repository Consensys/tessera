package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/transaction.feature",
        monochrome = true,
        plugin = {"progress"})
public abstract class CucumberTestCase {}
