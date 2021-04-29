package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.CucumberRawIT;
import com.quorum.tessera.test.CucumberRestIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.TestSuite;

import static com.quorum.tessera.config.CommunicationType.REST;
import static suite.EnclaveType.LOCAL;
import static suite.SocketType.HTTP;

@RunWith(TestSuite.class)
@TestSuite.SuiteClasses({
    MultipleKeyNodeIT.class,
    DeleteIT.class,
    PushIT.class,
    ReceiveIT.class,
    ReceiveRawIT.class,
    ResendAllIT.class,
    ResendIndividualIT.class,
    SendIT.class,
    SendRawIT.class,
    P2PRestAppIT.class,
    TransactionForwardingIT.class,
    CucumberRestIT.class,
    CucumberRawIT.class,
    OpenApiIT.class
})
@ProcessConfig(
    dbType = DBType.H2,
    communicationType = REST,
    enclaveType = LOCAL,
    admin = false,
    prefix = "",
    socketType = HTTP,
    encryptorType = EncryptorType.NACL)
public class RestSuiteSimple {
}
