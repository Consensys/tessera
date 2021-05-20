module tessera.enclave.cli {
  requires info.picocli;
  requires org.slf4j;
  requires tessera.cli.api;
  requires tessera.config;
  requires tessera.shared;

  exports com.quorum.tessera.enclave.server;

  opens com.quorum.tessera.enclave.server to
      info.picocli;

  uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;

  provides com.quorum.tessera.cli.CliAdapter with
      com.quorum.tessera.enclave.server.EnclaveCliAdapter;
}
