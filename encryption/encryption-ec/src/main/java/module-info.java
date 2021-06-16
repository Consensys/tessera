module tessera.encryption.ec {
  requires org.bouncycastle.provider;
  requires org.slf4j;
  requires tessera.encryption.api;

  provides com.quorum.tessera.encryption.EncryptorFactory with
      com.quorum.tessera.encryption.ec.EllipticalCurveEncryptorFactory;
}
