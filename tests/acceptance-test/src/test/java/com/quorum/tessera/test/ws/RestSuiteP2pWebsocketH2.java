package com.quorum.tessera.test.ws;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.rest.DeleteIT;
import com.quorum.tessera.test.rest.ReceiveIT;
import com.quorum.tessera.test.rest.ReceiveRawIT;
import com.quorum.tessera.test.rest.SendIT;
import com.quorum.tessera.test.rest.SendRawIT;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.SuiteClasses({SendIT.class, SendRawIT.class, DeleteIT.class, ReceiveIT.class, ReceiveRawIT.class})
@ProcessConfig(
        communicationType = CommunicationType.REST,
        p2pCommunicationType = "WEB_SOCKET",
        dbType = DBType.H2,
        socketType = SocketType.HTTP)
public class RestSuiteP2pWebsocketH2 {}
