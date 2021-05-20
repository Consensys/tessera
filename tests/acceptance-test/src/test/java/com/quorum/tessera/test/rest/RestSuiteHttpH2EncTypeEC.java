package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.CucumberRawIT;
import com.quorum.tessera.test.CucumberRestIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(
    communicationType = CommunicationType.REST,
    dbType = DBType.H2,
    socketType = SocketType.HTTP,
    encryptorType = EncryptorType.EC)
@TestSuite.SuiteClasses({
  SendReceivePrivacyGroupIT.class,
  PrivacyGroupIT.class,
  PrivacyIT.class,
  VersionIT.class,
  MultipleKeyNodeIT.class,
  DeleteIT.class,
  PushIT.class,
  ReceiveIT.class,
  ReceiveRawIT.class,
  ResendAllIT.class,
  ResendIndividualIT.class,
  SendIT.class,
  SendRawIT.class,
  com.quorum.tessera.test.rest.multitenancy.SendIT.class,
  com.quorum.tessera.test.rest.multitenancy.ReceiveIT.class,
  com.quorum.tessera.test.rest.multitenancy.PrivacyIT.class
})
public class RestSuiteHttpH2EncTypeEC {}
