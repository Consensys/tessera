package com.quorum.tessera.test.rest;


import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@TestSuite.ProcessConfig(
        communicationType = CommunicationType.REST,
        dbType = DBType.HSQL,
        socketType = SocketType.HTTP)

@Suite.SuiteClasses({
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
    AdminConfigIT.class,
    CucumberRestIT.class,
    CucumberRawIT.class,
    CucumberAdminIT.class
})
public class RestSuiteHSQL {
    
}
