module tessera.migration.multitenancy.main {
  requires tessera.cli.api;
  requires tessera.data;
  requires tessera.config;
  requires tessera.encryption.api;
  requires info.picocli;
  requires tessera.enclave.enclave.api.main;
  requires java.sql;
  requires java.persistence;

  opens com.quorum.tessera.multitenancy.migration to
      info.picocli;

  exports com.quorum.tessera.multitenancy.migration to
      info.picocli;
}
