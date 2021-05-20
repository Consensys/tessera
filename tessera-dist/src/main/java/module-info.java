module tessera.tessera.dist.main {
  exports com.quorum.tessera.launcher;

  requires java.validation;
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.cli.api;
  requires tessera.cli.config;
  requires tessera.config;
  requires tessera.enclave.enclave.api.main;
  requires tessera.server.jersey;
  requires tessera.server.api;
  requires tessera.tessera.context.main;
  requires tessera.transaction;
  requires tessera.partyinfo;
  requires tessera.shared.main;
  requires tessera.partyinfo.jaxrs;
  requires tessera.tessera.recover.main;
  requires java.json;
  requires tessera.server.jersey.unixsocket;
  requires org.bouncycastle.provider;

  uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
  uses com.quorum.tessera.p2p.resend.TransactionRequester;
  uses com.quorum.tessera.discovery.EnclaveKeySynchroniser;
  uses com.quorum.tessera.config.apps.TesseraApp;
  uses com.quorum.tessera.server.TesseraServerFactory;
  uses com.quorum.tessera.context.RuntimeContext;
  uses com.quorum.tessera.serviceloader.ServiceLoaderUtil;
}
