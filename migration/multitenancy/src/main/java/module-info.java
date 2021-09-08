module tessera.migration.multitenancy {
  requires tessera.cli.api;
  requires tessera.data;
  requires tessera.config;
  requires tessera.encryption.api;
  requires info.picocli;
  requires tessera.enclave.api;
  requires java.sql;
  requires jakarta.persistence;

  opens com.quorum.tessera.multitenancy.migration to
      info.picocli;

  exports com.quorum.tessera.multitenancy.migration to
      info.picocli;
}
