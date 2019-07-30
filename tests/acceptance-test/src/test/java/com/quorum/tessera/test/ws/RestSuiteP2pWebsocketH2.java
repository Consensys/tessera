package com.quorum.tessera.test.ws;

import com.quorum.tessera.test.rest.*;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(
        communicationType = CommunicationType.REST,
        p2pCommunicationType = "WEB_SOCKET",
        dbType = DBType.H2,
        socketType = SocketType.HTTP)
public class RestSuiteP2pWebsocketH2 extends RestSuite {}
