package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.ProcessConfig(
        communicationType = CommunicationType.GRPC,
        dbType = DBType.H2,
        socketType = SocketType.HTTP)
public class GrpcSuiteH2 extends GrpcSuite {


}
