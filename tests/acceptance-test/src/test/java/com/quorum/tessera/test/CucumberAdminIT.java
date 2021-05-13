package com.quorum.tessera.test;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "admin.cmd",
    features = "build/resources/test/features/admin.feature",
    monochrome = true,
    plugin = {"progress"})
public class CucumberAdminIT {}
