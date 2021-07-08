module tessera.encryption.kalium {
  requires kalium;
  requires org.slf4j;
  requires tessera.encryption.api;

  provides com.quorum.tessera.encryption.EncryptorFactory with
      com.quorum.tessera.encryption.nacl.kalium.KaliumFactory;
}
