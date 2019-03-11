package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.CucumberGprcIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.ProcessConfig(
        communicationType = CommunicationType.GRPC,
        dbType = DBType.HSQL,
        socketType = SocketType.HTTP)

@TestSuite.SuiteClasses({
    SendGrpcIT.class,
    PartyInfoGrpcIT.class,
    TesseraGrpcIT.class,
    CucumberGprcIT.class
})
public class GrpcSuiteHSQL {


}
