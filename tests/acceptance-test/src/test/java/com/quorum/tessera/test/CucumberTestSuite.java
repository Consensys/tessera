package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.cli.CucumberFileKeyGenerationIT;
import com.quorum.tessera.test.cli.CucumberVersionCliIT;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.SuiteClasses({
  CucumberRawIT.class,
  CucumberRestIT.class,
  CucumberWhitelistIT.class,
  CucumberFileKeyGenerationIT.class,
  CucumberVersionCliIT.class
})
@ProcessConfig(
    communicationType = CommunicationType.REST,
    dbType = DBType.H2,
    socketType = SocketType.HTTP,
    encryptorType = EncryptorType.NACL,
    prefix = "cucumber")
public class CucumberTestSuite {}
