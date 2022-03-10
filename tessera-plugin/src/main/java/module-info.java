module tessera.plugin {
  requires org.slf4j;
  requires tessera.shared;
  requires tessera.pluginAPI;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.partyinfo;
  requires org.pf4j;

  exports com.quorum.tessera.plugin;

}
