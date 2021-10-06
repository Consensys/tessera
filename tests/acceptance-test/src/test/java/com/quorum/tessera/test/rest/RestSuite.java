package com.quorum.tessera.test.rest;

import suite.TestSuite;

@TestSuite.SuiteClasses({
  SendMandatoryRecipientsIT.class,
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
  P2PRestAppIT.class,
  TransactionForwardingIT.class,
  CustomPayloadEncryptionIT.class,
  OpenApiIT.class,
  MetricsIT.class,
  ///
  com.quorum.tessera.test.rest.multitenancy.SendIT.class,
  com.quorum.tessera.test.rest.multitenancy.ReceiveIT.class,
  com.quorum.tessera.test.rest.multitenancy.PrivacyIT.class
})
public abstract class RestSuite {}
