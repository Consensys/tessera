
package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/send/send.feature",
    glue = "send.raw",
    tags = "@raw")
public class CucumberRawIT {
    
}
