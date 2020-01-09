package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "admin.cmd",
        features = "classpath:features/admin.feature",
        monochrome = true,
        plugin = {"progress"})
public class CucumberAdminIT {}
