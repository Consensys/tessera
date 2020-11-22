package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.CucumberRestIT;
import suite.TestSuite;

@TestSuite.SuiteClasses({
//    PrivacyIT.class,
    VersionIT.class,
//    MultipleKeyNodeIT.class,
//    DeleteIT.class,
//    PushIT.class,
//    ReceiveIT.class,
//    ReceiveRawIT.class,
//    ResendAllIT.class,
//    ResendIndividualIT.class,
//    SendIT.class,
//    SendRawIT.class,
//    P2PRestAppIT.class,
//    TransactionForwardingIT.class,
    CucumberRestIT.class,
//    CucumberRawIT.class,
//    CustomPayloadEncryptionIT.class
})
public abstract class RestSuite {
}
