module tessera.encryption.encryption.api.main {
  requires org.slf4j;
  requires tessera.shared.main;

  uses com.quorum.tessera.encryption.EncryptorFactory;

  exports com.quorum.tessera.encryption;
}
