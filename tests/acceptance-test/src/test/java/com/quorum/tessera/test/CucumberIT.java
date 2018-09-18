package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;
import org.junit.Ignore;

@Ignore
@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features",glue = "com.quorum.tessera.test.rest")
public class CucumberIT {
    
//    private static final ProcessManager PROCESS_MANAGER = new ProcessManager(CommunicationType.REST);
//
//        
//        
//   // @BeforeClass
//    public static void onSetup() throws Exception {
//                System.setProperty("application.jar","/Users/mark/Library/Maven/repo/com/quorum/tessera/tessera-app/0.7-SNAPSHOT/tessera-app-0.7-SNAPSHOT-app.jar");
//
//        
//        PROCESS_MANAGER.startNodes();
//    }
//
//    //@AfterClass
//    public static void onTearDown() throws Exception {
//        PROCESS_MANAGER.stopNodes();
//
//    }
    
}
