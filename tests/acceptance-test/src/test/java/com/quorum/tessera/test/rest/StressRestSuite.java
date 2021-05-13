package com.quorum.tessera.test.rest;

import static com.quorum.tessera.config.CommunicationType.REST;
import static suite.SocketType.HTTP;

import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.TestSuite;

@TestSuite.SuiteClasses({StressSendIT.class})
@RunWith(TestSuite.class)
@ProcessConfig(
    dbType = DBType.H2,
    socketType = HTTP,
    communicationType = REST,
    admin = false,
    prefix = "",
    encryptorType = EncryptorType.NACL)
public class StressRestSuite {}
