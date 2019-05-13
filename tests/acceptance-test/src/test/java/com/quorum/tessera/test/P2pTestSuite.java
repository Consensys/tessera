package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import org.junit.runner.RunWith;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.ProcessConfig(
    communicationType = CommunicationType.REST,
    socketType = SocketType.HTTP,
    dbType = DBType.H2,
    prefix = "p2p"
)
@TestSuite.SuiteClasses(PeerToPeerIT.class)
public class P2pTestSuite {
}
