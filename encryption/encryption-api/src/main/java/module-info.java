module tessera.encryption.api {
  requires org.slf4j;
  requires tessera.shared;

  uses com.quorum.tessera.encryption.EncryptorFactory;

  exports com.quorum.tessera.encryption;
}
