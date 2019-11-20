package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.CucumberGprcIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.EnclaveType;
import suite.TestSuite;

import suite.ProcessConfig;
import suite.SocketType;

@TestSuite.SuiteClasses({SendGrpcIT.class, PartyInfoGrpcIT.class, TesseraGrpcIT.class, CucumberGprcIT.class})
@RunWith(TestSuite.class)
@ProcessConfig(
        communicationType = CommunicationType.GRPC,
        dbType = DBType.H2,
        enclaveType = EnclaveType.LOCAL,
        socketType = SocketType.HTTP)
public class GrpcSuite {}
