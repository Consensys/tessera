package com.quorum.tessera.test.grpc;

import com.quorum.tessera.test.CucumberGprcIT;
import suite.TestSuite;

@TestSuite.SuiteClasses({
    SendGrpcIT.class,
    PartyInfoGrpcIT.class,
    TesseraGrpcIT.class,
    CucumberGprcIT.class
})
public abstract class GrpcSuite {
    
}
