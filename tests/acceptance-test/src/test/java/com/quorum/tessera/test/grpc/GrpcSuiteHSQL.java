package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(communicationType = CommunicationType.GRPC, dbType = DBType.HSQL, socketType = SocketType.HTTP)
public class GrpcSuiteHSQL extends GrpcSuite {}
