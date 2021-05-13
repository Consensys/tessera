module tessera.migration.multitenancy.main {
  requires tessera.cli.cli.api.main;
  requires tessera.tessera.data.main;
  requires tessera.config.main;
  requires tessera.encryption.encryption.api.main;
  requires info.picocli;
  requires tessera.enclave.enclave.api.main;
  requires java.sql;
  requires java.persistence;

  opens com.quorum.tessera.multitenancy.migration to
      info.picocli;

  exports com.quorum.tessera.multitenancy.migration to
      info.picocli;
}
