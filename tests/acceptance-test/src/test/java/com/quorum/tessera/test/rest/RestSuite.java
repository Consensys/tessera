package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.CucumberRawIT;
import com.quorum.tessera.test.CucumberRestIT;
import suite.TestSuite;

@TestSuite.SuiteClasses({
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
    P2PRestAppIT.class,
    TransactionForwardingIT.class,
    CucumberRestIT.class,
    CucumberRawIT.class,
    CustomPayloadEncryptionIT.class,
    OpenApiIT.class,
    ///
    com.quorum.tessera.test.rest.multitenancy.SendIT.class,
    com.quorum.tessera.test.rest.multitenancy.ReceiveIT.class,
    com.quorum.tessera.test.rest.multitenancy.PrivacyIT.class
})
public abstract class RestSuite {}
