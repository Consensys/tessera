package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(
    communicationType = CommunicationType.REST,
    dbType = DBType.H2,
    socketType = SocketType.HTTP,
    encryptorType = EncryptorType.NACL,
    prefix = "p2p")
@TestSuite.SuiteClasses(PeerToPeerIT.class)
public class P2pTestSuite {}
