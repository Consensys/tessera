package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.EnclaveType;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(
        communicationType = CommunicationType.REST,
        dbType = DBType.H2,
        socketType = SocketType.HTTP,
        enclaveType = EnclaveType.REMOTE)
public class RestSuiteRemoteEnclaveH2 extends RestSuite {}
