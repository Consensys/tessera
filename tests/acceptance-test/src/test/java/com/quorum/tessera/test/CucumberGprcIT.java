
package com.quorum.tessera.test;


import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/send/send.feature",
    glue = "send.grpc",
    tags = "@grpc")
public class CucumberGprcIT {

}
