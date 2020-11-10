package com.quorum.tessera.test;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "admin.cmd",
    features = "classpath:features/admin.feature",
    monochrome = true,
    plugin = {"progress"}
)
public class CucumberAdminIT {

}
