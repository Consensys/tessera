package com.quorum.tessera.test.ws;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
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
@TestSuite.SuiteClasses({SendRawIT.class, SendIT.class, DeleteIT.class, ReceiveIT.class, ReceiveRawIT.class})
@ProcessConfig(
        communicationType = CommunicationType.REST,
        p2pCommunicationType = "WEB_SOCKET",
        dbType = DBType.H2,
        socketType = SocketType.HTTP,
        p2pSsl = true,
        encryptorType = EncryptorType.NACL)
public class RestSuiteP2pWebsocketH2 {}
