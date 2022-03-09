module tessera.plugin {
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;
  requires tessera.context;
  requires org.pf4j;

  exports com.quorum.tessera.plugin;
}
