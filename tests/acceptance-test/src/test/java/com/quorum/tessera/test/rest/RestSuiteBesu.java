package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.SuiteClasses({PrivacyGroupIT.class, SendReceiveBesuIT.class})
@ProcessConfig(
    communicationType = CommunicationType.REST,
    dbType = DBType.H2,
    socketType = SocketType.HTTP,
    encryptorType = EncryptorType.NACL,
    clientMode = ClientMode.ORION)
public class RestSuiteBesu extends RestSuite {}
