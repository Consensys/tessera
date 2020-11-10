package com.quorum.tessera.test;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = {"classpath:features/transaction.feature"},
    monochrome = true,
    plugin = {"progress"}
)
public abstract class CucumberTestCase {

}
